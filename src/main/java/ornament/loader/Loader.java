package ornament.loader;

import ornament.Subject;
import ornament.Subscriber;
import ornament.annotation.Topic;
import ornament.annotation.Watch;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public class Loader {
    private volatile static   Loader loader=null;
    private HashMap<String, Object>  observableMap=new HashMap<>();
    private HashMap<String,List<Object>>  observeMap=new HashMap<>();
    private List<Class<?>> observables=null;
    private Loader(){ }

    public static Loader getInstance(){
        if(loader==null){
            synchronized (Loader.class){
                if(loader==null){
                    loader=new Loader();
                }
            }
        }
        return loader;
    }

    //获取类加载地址
    private String getRealPath(String originUrl){
        StringBuilder result=new StringBuilder();
        boolean  tag=false;
        String [] orign=originUrl.replace("\\",".").split("\\.");
        for(int i=0;i<orign.length;i++){
            if(i>1&& orign[i-1].equals("classes"))tag=true;
            if(tag && i<(orign.length-1)){
                if(i==(orign.length-2)){
                    result.append(orign[i]);
                }else{
                    result.append(orign[i]+".");
                }
                continue;
            }
        }
        return result.toString();
    }

    //加载所有class文件
    private void  getClasses(Class<?> class1) throws ClassNotFoundException {
        List<Class<?>>  classes=new ArrayList<>();
        boolean recursive=true;
        String path = class1.getClassLoader().getResource("").toString();
        int m = path.indexOf("/");// file:/<----点位到file:后面的反斜杠
        path = path.substring(m + 1);// 从反斜杠之后的一位开始截取字符串
        String packageDirName = path.replace('.', '/');
        File file = new File(path);
        if(file.exists()){
            LinkedList<File> lis=new LinkedList<>();
            File[] files=file.listFiles();
            for(File file1:files){
                if(file1.isDirectory()){
                    lis.add(file1);
                }else{
                    String [] dirNames=file1.getName().split("\\.");
                    if(dirNames.length>=1&& dirNames[dirNames.length-1].equals("class")){
                        String url=  getRealPath(file1.getAbsolutePath());
                        Class clas=Thread.currentThread().getContextClassLoader().loadClass(url);
                        proccessClass(clas);
                    }
                }
            }
            File temp_file;
            while(!lis.isEmpty()){
                temp_file=lis.removeFirst();
                files=temp_file.listFiles();
                for(File file1:files){
                    if(file1.isDirectory()){
                        lis.add(file1);
                    }else{
                        String [] dirNames=file1.getName().split("\\.");

                        if(dirNames.length>=1 && dirNames[dirNames.length-1].equals("class")){
                            String url=  getRealPath(file1.getAbsolutePath());
                            Class clas=Thread.currentThread().getContextClassLoader().loadClass(url);
                            proccessClass(clas);
                        }
                    }
                }
            }
        }
    }

    //判断该类是否有注解
    private  boolean initialVerifyClass(Class<?> clazz){
        Annotation[] annotations=clazz.getAnnotations();
        if(annotations.length!=0)return true;
        return false;
    }

    private void proccessClass(Class<?> clazz){
        if(!initialVerifyClass(clazz))return;
        verifyObservable(clazz);
        verifyObserve(clazz);
    }


    private void registeAll(){
        Iterator iter=observeMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry= (Map.Entry) iter.next();
            ArrayList<Object> lis= (ArrayList<Object>) entry.getValue();
                if(lis!=null){
                    for(Object o:lis){
                        register((Subscriber) o, (String) entry.getKey());
                    }
            }

        }
    }
    public void register(Subscriber subscriber,String topic){
        Subject observable= (Subject) observableMap.get(topic);
        if(observable==null){
            System.out.println("can't find the topic:"+topic);
        }else observable.register(subscriber);

    }

    //检查class文件是否含有标注有指定观察主题注解的类
    private String verifyClass(Class<?> clazz, Class<?> clasz){
            Annotation[] annotations=clazz.getAnnotations();
            for(Annotation anno:annotations){
                if(anno.annotationType().equals(clasz)){

                }
            }
        return "topic";
    }
    private void verifyObservable(Class<?> clazz){
        Topic observable=clazz.getAnnotation(Topic.class);
        if(observable!=null){
               String topic= observable.name();
               if(topic!=null && topic!=""){
                   try {
                       observableMap.put(topic, BehaviorEnhancer.getInstance().observableEnhancer((Subject) clazz.newInstance(),topic));
                   } catch (InstantiationException e) {
                       e.printStackTrace();
                   } catch (IllegalAccessException e) {
                       e.printStackTrace();
                   }
               }
         }
    }

    private void verifyObserve(Class<?> clazz){
        Watch observe=clazz.getAnnotation(Watch.class);
        if(observe!=null){
            String topic= observe.target();
            if(topic!=null && topic!=""){
                try {
                    if(observeMap.get(topic)==null){
                        observeMap.put(topic,new ArrayList<>());
                    }
                    observeMap.get(topic).add(clazz.newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public Object findSubject(String topic){
        Iterator iter=observableMap.entrySet().iterator();
        Object obj=null;
        while(iter.hasNext()){
            Map.Entry entry= (Map.Entry) iter.next();
            if(entry.getKey().equals(topic)){
                obj=  entry.getValue();
                break;
            }
        }
        return  obj;
    }

    public Object findSubscriberByName(String name){
        Iterator iter=observeMap.entrySet().iterator();
        List<Object> objs=null;
        Object res=null;
        while(iter.hasNext() && res==null){
            Map.Entry entry= (Map.Entry) iter.next();
            objs= (List<Object>) entry.getValue();
            if(objs!=null){
                for(Object o:objs){
                  Watch   wh= ((Subscriber)o).getClass().getAnnotation(Watch.class);
                  if(wh.name().equals(name)){
                      res= o;
                      break;
                  }
                }
            }
        }
        return  res;
    }

    public Object findSubscriberByTopicAndName(String topic,String name){
        List<Object> lis=observeMap.get(topic);
        Object res=null;
        if(lis!=null){
            for(Object o:lis){
                Watch   wh= ((Subscriber)o).getClass().getAnnotation(Watch.class);
                if(wh.name().equals(name)){
                    res= o;
                    break;
                }
            }
        }
        return  res;
    }


    public Loader init(Class<?> class1) {
        try {
            getClasses(class1);
            registeAll();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return loader;
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Loader loader=Loader.getInstance().init(Loader.class);
    }

}
