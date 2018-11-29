# Gadtry [![Build Status](http://img.shields.io/travis/harbby/gadtry.svg?style=flat&branch=master)](https://travis-ci.org/harbby/gadtry)
Gadtry A collection of java tool libraries.
Contains: ioc. exec .graph 

## Use
* maven
```xml
    <dependency>
      <groupId>com.github.harbby</groupId>
      <artifactId>gadtry</artifactId>
      <version>1.0.0</version>
    </dependency>
```
* gradle
```groovy
    compile group: 'com.github.harbby', name: 'gadtry', version: '1.0.0'
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