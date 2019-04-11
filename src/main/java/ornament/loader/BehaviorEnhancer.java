package ornament.loader;


import ornament.Subject;

public class BehaviorEnhancer   {
    private static volatile  BehaviorEnhancer enhancer;



    private BehaviorEnhancer(){}

    public static BehaviorEnhancer getInstance(){
        if(enhancer==null){
            synchronized (BehaviorEnhancer.class){
                if(enhancer==null){
                    enhancer=new BehaviorEnhancer();
                }
            }
        }
        return  enhancer;
    }


    public Subject observableEnhancer(Object observable, String topic)  {
        return (Subject) new ObservableInvocationHandler().bind(observable,topic);
    }






}
