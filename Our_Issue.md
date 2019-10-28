# Issue

```
Description:

Parameter 0 of method blobStore in org.superbiz.moviefun.Application required a bean of type 'org.superbiz.moviefun.blobstore.ServiceCredentials' that could not be found.

The injection point has the following annotations:
	- @org.springframework.beans.factory.annotation.Autowired(required=true)
```

We added @Bean definition of ServiceCredentials before BlobStore definition in Application.java because BlobStore uses ServiceCredentials. 