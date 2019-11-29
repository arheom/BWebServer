# Configuration options
```
# Server port
port = 8080
# Number of threads if using newFixedThreadPool executor
nThreads = 100
# connection timeout when working in keep alive mode
connectionTimeout = 5000
# root folder from where the content is served
contentRootPath = C:\\Projects\\github\\BWebServer\\contentdb
# max CPU usage from when the server will start to not accept connections anymore
maxCPUUsage = 80
# Min log level
logLevelMin = Info
```
**contentRootPath** needs to be configured with the correct place where the content is served from. In the project file, this corresponds to the contentdb folder.

# Load Tests
The project was tested with https://artillery.io/. In order to install it, run:
`$ npm install -g artillery` 
To run the tests, navigate to the \loadtest\artillery folder and run 
`artillery run .\config-get.yaml` with the appropriate config file. 

# Server Features
1. **Multi-threading** static content web server, with a configurable number of threads in the ThreadPool and async requests to content service.
2. **File content service** with ReadWrite locks on content, to ensure 2 threads cannot write in same file in same time, but readers can access them.
3. **Simple template engine** to decouple the rendering of the HTML from the content and server. The templates folder (located in the current running context), will contain rendering templates with replacements tokens.
3. **Health service** - the server is integrated with very basic heart beat checks (CPU only for the time being). When the server health is bad, the web server will reject new connections, to allow the NLB to switch to another server, helping ensuring the SLA response time.
4. **Control plane service** - empty control place. When problems arrive in production and troubleshooting is needed, this service allow to quickly inject and change logic and advanced properties of running server, for example if another relying service is hit by this one and doing very bad, and the settings are not exposed by config, with control plane can be changed fast to resolve as a hotfix the problem on production, to reduce the load this server will make to the relying service.
5. **Decoupled architecture**. All services can be very easily changed, for example having multiple content services and dynamically choosing one of them to resolve user request for content. Also, architecture is decoupled also for implementing http client capabilities and server policies:
- **Client capabilities**: for the time being, ConnectionClose and KeepAlive capabilities were implemented with this concept. The capabilities will be detected and will inject their requirements inside the response sent to client. The implementation of the capabilities is decoupled from the web server logic.
- **Server policies**: similar concept as the client capabilities, but purposed to ensure some rules to the whole http context. ContentType (only supporting for the time being text/html), ContentLength and Date response headers were implemented this way.
New policies and capabilities can be added to the server, without affecting the server logic. All logic of them is done in their associated class.

6. **Directory listing** - implemented with the template engine feature, will list the current folder's files as HTML.
7. Only HTTP **GET, PUT, POST and DELETE** were implemented for the time being.

# Roadmap
- Implement all **HTTP (1.0, 1.1, 2.0) specifications** (as capabilities and policies)
- **Priority Queue** for the server requests, to take first "premium user IPs" and last the free users.
- Improving content service performance, by decoupling the web server number of requests from the content service threads, with a **pubsub thread safe pattern**. Also, move it out form the web server process.
- **TraceService** with implementation for **https://opentracing.io/**
- **Rate Limiting**
- **HTTPs**
- **OpenID Connect** integration
- **Server Cache**
- **Distributed server** capabilities, with session info sync, cache sync
- Other content services: **DB content service**, **Search content service** (Elastic Search integration), **Aggregated content service** (templates from content service, customization from database content service)...
