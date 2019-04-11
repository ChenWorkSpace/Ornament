# Ornament
  An event handling plug-in can help classes acquire the ability to be observed through simple settings
  > projects depend upon  cglib When you use this library, you need to introduce it.
  
  ## usage

> 1.Introducing dependency：
```html
    <dependencies>
        <dependency>
            <groupId>cglib</groupId>
            <artifactId>cglib</artifactId>
            <version>3.2.10</version>
        </dependency>
    </dependencies>
```
> 2.Write the Subject
```html
  @Topic(name = "topic1")
public class Topic1 implements Subject {
    @Override
    public Subscriber register(Subscriber subscriber) {
        return subscriber;
    }

    @Override
    public Object notification(Object object) {
        return object;
    }

    @Override
    public Subscriber remove(Subscriber subscriber) {
        return subscriber;
    }
}
```
> 3 Write the Subscriber  
```html

@Watch(target = "topic1",name = "watch1")
public class Watch1 implements Subscriber {
    @Override
    public void update(Object object) {
        System.out.println("update:"+object);
    }
}
```

> 4 Invocation style 
```html
   public static void main(String[] args) {
        Loader loader =Loader.getInstance().init(Watch1.class);
        Topic1 t1= (Topic1) loader.findSubject("topic1");
        Watch1 w1= (Watch1) loader.findSubscriberByTopicAndName("topic1","watch1");
        t1.notification("hello");
        t1.remove((Subscriber) w1);
        t1.notification("hello1");
        t1.register((Subscriber) w1);
        t1.notification("hello2");
    }

```
