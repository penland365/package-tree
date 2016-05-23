## Design
By far, the overriding factor in this design choice was the requirement to only use my language's Standard Library.

Given that requirement, and the relatively low number of concurrent clients required, the decision was made to avoid an asynchronous framework that something like java.nio or Netty of Finagle would provide.

A traditional "thread per connection" design serves two purposes:
1. It keeps complexity at a minimum
2. It can easily scale to an order of magnitude greater in requests with no degradation ( repeated tests of 1000 concurrent clients at an unluckiness of 80% passed with no issues. For a JVM app, the memory footprint of ~1.3G was surprisingly low.)

A fixed thread pool was introduced to allow the run time in effectively limit the number of connections without harming the consistency of the package server itself. If the thread pool is exhausted, any incoming connections simply time out - to the consternation of the client, but with no harm done to the package server.

The Package Tree is handled by a traditional Map ( albeit a Concurrent JVM map ). Given the requirements, optimizing for easy and simplicity seemed paramount over an attempt to implement an optimized Data Structure for the task.
