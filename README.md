# Gadtry [![Build Status](http://img.shields.io/travis/harbby/gadtry.svg?style=flat&branch=master)](https://travis-ci.org/harbby/gadtry)
Gadtry A collection of java tool libraries.
Contains: ioc. aop. exec. graph ...

## Use
* maven
```xml
<dependency>
  <groupId>com.github.harbby</groupId>
  <artifactId>gadtry</artifactId>
  <version>1.2.0</version>
</dependency>
```

## Ioc
Create Factory:
```
IocFactory iocFactory = IocFactory.create(binder -> {
    binder.bind(Set.class).by(HashSet.class).withSingle();
    binder.bind(HashSet.class).withSingle();
    binder.bind(List.class).byCreator(ArrayList::new);  //Single object
    binder.bind(Object.class, new Object());
    binder.bind(Map.class).byCreator(HashMap::new).withSingle();  //Single object
    binder.bind(TestInject.class);
});

Set a1 = iocFactory.getInstance(Set.class);
Set a2 = iocFactory.getInstance(Set.class);
Assert.assertEquals(true, a1 == a2); // Single object
```
Class Inject
```
public class TestInject
{
    @Autowired
    private TestInject test;

    @Autowired
    public TestInject(HashMap set){
        System.out.println(set);
    }
}
```

## Aop
Does not rely on ioc containers:
```
T proxy = AopFactory.proxy(Class<T>)
    .byInstance(instance)
    .returnType(void.class, Boolean.class)
    //.methodAnnotated(Override.class)
    .around(proxyContext -> {
            String name = proxyContext.getInfo().getName();
            System.out.println(name);
            Object value = proxyContext.proceed();
            switch (name) {
                case "add":
                    Assert.assertEquals(true, value);  //Set or List
                    break;
                case "size":
                    Assert.assertTrue(value instanceof Integer);
                    break;
            }
    });
```
Dependent on ioc container:
```
        IocFactory iocFactory = GadTry.create(binder -> {
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();
            binder.bind(HashSet.class).by(HashSet.class).withSingle();
        }).aop(binder -> {
            binder.bind("point1")
                    .withPackage("com.github.harbby")
                    //.subclassOf(Map.class)
                    .classAnnotated()
                    .classes(HashMap.class, HashSet.class)
                    .whereMethod(methodInfo -> methodInfo.getName().startsWith("add"))
                    .build()
                    .before((info) -> {
                        Assert.assertEquals("add", info.getName());
                        System.out.println("before1");
                    })
                    .after(() -> {
                        Assert.assertTrue(true);
                        System.out.println("after2");
                    });
        }).initialize();

        Set set = iocFactory.getInstance(HashSet.class);
```

## Exec New Jvm
Throw the task to the child process
```
JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
    .setCallable(() -> {
        // this is child process
        System.out.println("************ runing your task ***************");
        return 1;
    })
    .addUserjars(Collections.emptyList())
    .setXms("16m")
    .setXmx("16m")
    .setConsole((msg) -> System.out.println(msg))
    .build();

VmFuture<Integer> out = launcher.startAndGet();
Assert.assertEquals(out.get().get().intValue(), 1);
```

## Useful mailing lists
1. yezhixinghai@gmail.com - For discussions about code, design and features

## Other
* 加入QQ群 438625067