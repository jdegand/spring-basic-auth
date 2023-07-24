# Spring Basic Auth

Basic Spring User Authentication based off this [playlist tutorial by Bingyang Wei](https://www.youtube.com/watch?v=asS2kcalidY&list=PLqq9AhcMm2oPdXXFT3fzjaKLsVymvMXaY&index=1).  

The video series is high quality and watching it pushed me to examine the approaches I use in my Spring Boot applications.  

## Built With

- Java 17
- Spring Boot 3
- H2 Database
- Spring Boot Starter Oauth2 Resource Server
- Spring Data JPA
- Spring Security Test

## Thoughts

- The tutorial uses a Structure by Feature approach.  I use a Structure by Layer approach.  In reading about [the two approaches](https://www.geeksforgeeks.org/spring-boot-code-structure/), I think it is important I try to adopt Stucture by Feature as it greatly helps productivity with easier code reuse.  I intend to reference / rework the basic authentication in this repo in future projects.
- Adding final to injected services and repositories is a quick and easy fix to improve code quality.  
- Using records with DTO conversion is a better solution to what I've done previously.  Records are read-only and this helps the security and the reliability of an application.  
- Adding @Transactional to services helps rollback failed database operations.  
- The DBDataInitializer was surprisingly simple to add so I added a similar one to my [LCOGT-spring-backend](https://github.com/jdegand/LCOGT-spring-backend).  
- I had to make some modifications to the SecurityConfiguration to replace deprecated methods.  
- Adding Serializable to entities can be [problematic](https://stackoverflow.com/questions/2020904/when-and-why-jpa-entities-should-implement-the-serializable-interface).
- Removed standard Result class for Response Entity or standard entity object
- Result class - 3/4 of the class is redundant - although having a standard response to every endpoint can be beneficial (especially for testing).
- Did not add any validation to User entity fields - this prevents handleValidationException from being invoked.  You can also not test failure paths for updateUser or addUser.
- Added a changePassword route - used "reset" for the path name - whatever it is named, you need to update security configuration to allow the route.  
- Thought about adding the reset route to its own controller but I decided to leave it in the user controller.  
- `ChangePasswordRequest` could go in the DTO package.  
- Just returned a string saying "Password changed successfully" for success of changePassword method.  If you want to return user back, you need to convert the user object to a user DTO so you don't send the password back to the client. 
- I used a UserPrincipal object to help change the password so I could reuse `loadUserByUsername`. I used username as my unique identifier in the ChangePasswordRequest class.    
- I had to mock 3 methods to get the updatePasswordSuccess test to pass.  
- You get can the failure test for updatePassword to pass without having a correct test.  If you don't mock certain methods, it will fail regardless.  
- Didn't test the CORS configuration by making a request from a frontend.

## Improvements

- Validation
- Failure path tests
- Delete user returns void - could use a Response Entity and return "User {id} deleted successfully" 
- Lombok - parallel move?
- Refresh JWT Tokens 

## Useful Resources

- [Mageddo](https://mageddo.com/tools/yaml-converter) - YAML Converter
- [Geeks for Geeks](https://www.geeksforgeeks.org/spring-boot-code-structure/) - spring boot code structure
- [Stack Overflow](https://stackoverflow.com/questions/2020904/when-and-why-jpa-entities-should-implement-the-serializable-interface) - jpa entities should implement serializable interface?
- [Baeldung](https://www.baeldung.com/java-record-keyword) - record
- [YouTube](https://www.youtube.com/watch?v=B5Zrn1Tzyqw) - Spring ResponseEntity - How to customize the response in Spring Boot
- [BezKoder](https://www.bezkoder.com/spring-boot-refresh-token-jwt/) - spring boot refresh token jwt
- [Stack Overflow](https://stackoverflow.com/questions/4350874/unable-to-use-table-named-user-in-postgresql-hibernate) - unable to use table named user in postgresql hibernate
- [Blog](https://www.buggybread.com/2015/03/spring-framework-list-of-exceptions.html) - spring framework list of exceptions
- [Reflectoring](https://reflectoring.io/spring-boot-exception-handling/) - spring boot exception handling
- [Blog](https://www.roshanadhikary.com.np/2022/10/spring-boot-mvc-test.html) - spring boot mvc test
- [YouTube](https://www.youtube.com/watch?v=CB32_mdgXq8) - How to Implement Change Password Functionality in Spring Security | Spring Boot