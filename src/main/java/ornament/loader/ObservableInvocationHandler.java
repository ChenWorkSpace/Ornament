package ornament.loader;




import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import ornament.Subscriber;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ObservableInvocationHandler implements MethodInterceptor  {
    private String  topic;
    private Set<Subscriber> observes=new HashSet<>();
    private Object object;





    public Object bind(Object o,String topic){

        this.object=o;
        Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(this.object.getClass());
        enhancer.setCallback(this);

        return  enhancer.create();
    }



    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object invoke;
        if(method.getName().equals("notification")){
            for(Subscriber observe:observes){
                observe.update(objects[0]);
            }
        }
        invoke= method.invoke(object,objects);
        if(method.getName().equals("register")){
            if(!observes.contains(invoke)){
                observes.add((Subscriber) invoke);
            }
        }
        if(method.getName().equals("remove")){
            if(observes.contains(invoke)){
                observes.remove((Subscriber) invoke);
            }
        }
        return invoke;
    }





}

