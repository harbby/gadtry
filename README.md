# Gadtry [![Build Status](http://img.shields.io/travis/harbby/gadtry.svg?style=flat&branch=master)](https://travis-ci.org/harbby/gadtry) [![codecov](https://codecov.io/gh/harbby/gadtry/branch/master/graph/badge.svg)](https://codecov.io/gh/harbby/gadtry)

Welcome to gadtry !

Gadtry A collection of java tool libraries.
Contains: ioc. aop. mock. exec. graph ...

## Use
* maven
```xml
<dependency>
  <groupId>com.github.harbby</groupId>
  <artifactId>gadtry</artifactId>
  <version>1.8.3</version>
</dependency>
```

## Ioc
Create Factory:
```
IocFactory iocFactory = IocFactory.create(binder -> {
    binder.bind(Set.class).by(HashSet.class).withSingle();
    binder.bind(HashSet.class).withSingle();
    binder.bind(List.class).byCreator(ArrayList::new);  //No Single object
    binder.bind(Map.class).byCreator(HashMap::new).withSingle();  //Single object
    binder.bind(TestInject.class).noScope();
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
support concentric ring model
```
List<String> actions = new ArrayList<>();
Set set = AopGo.proxy(Set.class)
        .byInstance(new HashSet<>())
        .aop(binder -> {
            binder.doBefore(before -> {
                actions.add("before1");
            }).when().size();
            binder.doAround(cut -> {
                actions.add("before2");
                int value = (int) cut.proceed() + 1;
                actions.add("before3");
                return value;
            }).whereMethod(method -> method.getName().startsWith("size"));
            binder.doBefore(before -> {
                actions.add("before4");
            }).when().size();
        })
        .build();
Assert.assertEquals(set.size(), 1);
Assert.assertEquals(MutableList.of("before1", "before2", "before4", "before3"), actions);
```
can also be combined with ioc container
```
IocFactory iocFactory = GadTry.create(binder -> {
    binder.bind(Map.class).byCreator(HashMap::new).withSingle();
    binder.bind(HashSet.class).by(HashSet.class).withSingle();
}).aop(binder -> {
    binder.bind(HashSet.class).aop(binder1 -> {
        binder1.doAfter(methodInfo -> {
            System.out.println("after2");
        }).whereMethod(methodInfo -> methodInfo.getName().startsWith("add"));
        binder1.doBefore((info) -> {
            Assert.assertEquals("add", info.getName());
            System.out.println("before1");
        }).methodName("add");
    });
}).setConfigurationProperties(MutableMap.of())
        .initialize();

Set set = iocFactory.getInstance(HashSet.class);
```

## Multiprocessing Exec Fork New Jvm
Throw the task to the child process, 
```
JVMLauncher<Integer> launcher = JVMLaunchers.<Integer>newJvm()
    .setCallable(() -> {
        // this is child process
        System.out.println("************ runing your task ***************");
        return 1;
    })
    .setName("set this jvm jps name")   //set fork jvm jps name
    .setEnvironment("TestEnv", envValue)  //set Fork Jvm Env
    .addUserjars(Collections.emptyList())
    .setXms("16m")
    .setXmx("16m")
    .setConsole((msg) -> System.out.println(msg))
    .build();

Integer out = launcher.startAndGet();
Assert.assertEquals(out.intValue(), 1);
```

## Graph
* Create ImmutableGraph
```
Graph graph = ImmutableGraph.builder()
                .addNode("Throwable")
                .addNode("Exception")
                .addNode("IOException")
                .addNode("FileNotFoundException")

                .addNode("RuntimeException")
                .addNode("UnsupportedOperationException")
                .addNode("IllegalArgumentException")

                .addNode("Error")
                .addNode("OutOfMemoryError")
                .addNode("NoClassDefFoundError")

                .addEdge("Throwable", "Exception")
                .addEdge("Throwable", "Error")

                .addEdge("Exception", "IOException")
                .addEdge("Exception", "FileNotFoundException")
                .addEdge("Exception", "RuntimeException")
                .addEdge("RuntimeException", "UnsupportedOperationException")
                .addEdge("RuntimeException", "IllegalArgumentException")

                .addEdge("Error", "OutOfMemoryError")
                .addEdge("Error", "NoClassDefFoundError")
                .create();
```
* Print Graph:
```
graph.printShow("Throwable").forEach(System.out::println);

/
└────Throwable
     ├────Error
     │    ├────NoClassDefFoundError
     │    └────OutOfMemoryError
     └────Exception
          ├────RuntimeException
          │    ├────IllegalArgumentException
          │    └────UnsupportedOperationException
          ├────FileNotFoundException
          └────IOException
```
*  Search Graph:        
Demo: Search for routes with A to C distances less than 30:
```
        Graph<Void,EdgeData> graph = ...create...
        List<Route<Void, EdgeData>> routes = graph.searchRuleRoute("A", "C", route -> {
            long distances = getRouteDistance(route);
            return distances < 30;
        });
```


## Useful mailing lists
* yezhixinghai@gmail.com - For discussions about code, design and features
