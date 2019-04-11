package ornament;

public interface Subject {
    public Subscriber register(Subscriber subscriber);
    public Object notification(Object object);
    public Subscriber remove(Subscriber subscriber);
}
