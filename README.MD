
# Task description
Implement REST API where a customer can
execute next actions:
- get a list of currencies used in the project;
- get exchange rates for a currency;
- add new currency for getting exchange rates.

## Implementation notes
Stack:
- java 17
- spring data JPA (hibernate)
- spring webflux
- postgresql
- liquibase
- JUnit5, Mockito, WireMock, RestAssured, testcontainers, ProjectReactorTest

Integration with external public exchange-rates: https://currencylayer.com.

## How to run locally
Build project:
> mvn clean install

Boot postgres container:
> docker compose up

Pass your api_key from https://currencylayer.com: 
> export COM_FAST_HANDS_TEST_TASK_CLIENT_APILAYER_API_KEY=<your_api_key>

Notes:
1. Initially there are no currencies available, you need to add the required ones via available endpoints (check openapi.yaml).
2. All exchange-rate info is stored within in-memory-cache, so you need to wait for cron job, to trigger currencies update and populate cache.
By default it will be triggered each 2 minutes.
4. currencylayer free-plan applies following limitations:
- The base currency is fixed to USD, meaning you can get precise exchange rates from USD to other currencies.
- To calculate exchange rates for currencies other than USD,
the application performs basic computations using the base USD exchange rate, which may result in reduced precision.  