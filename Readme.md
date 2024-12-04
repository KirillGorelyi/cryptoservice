Initially, once the application is started, configuration file loads pre-saved existing values
(provided in the test task description, path is /src/main/resources/init_csv), in the current
version - these price files are treated as absolute truth and during uploading new files
you will not be able to rewrite these values, so the whole situation about files - choose wisely.
As expansion plans for the service
1. Adding functionality of live fetching data from crypto markets
like Binance or ByBit or whatever, and then allowing user to compare uploaded data with data from service
and giving an ability to choose which one is source of truth
2. Adding any basic authentication to avoid using some random library from GitHub for restricting endpoint 
rate limit. And a user model based on subscription principals to control rate limit 
(e.g., 100 requests/min - 5 USDT, 200 requests/min - 10 USDT)  
3. Add some math here - at least basic models like AutoRegression, Moving Average, SARMA etc. or even 
some custom neural networks
4. Adjust and improve load tests

Base mapping for backend services is /api. The rate limit is 10 requests per minute from each unique ip. If you will to change the limit, 
please correct it in the class RateLimitingFilter.\
Backend service contains six endpoints; 1 for uploading data and 5 for data calculation.\
Uploading endpoint an additional path /csv/upload: plain and simple, upload the csv file, and you are good to go. 
In case the coin that you are uploading is not present in the system, it will be created.
In case you have more than one coin in the file, do not worry—the system will create new coins, 
and you can refer to them later. \
\
Calculation endpoints:

1. /all/normalized-range - retrieves a descending sorted list of all coins based on their normalized range.
Cached for efficiency.

2. /{coin}/info - returns the oldest, newest, minimum, and maximum prices for the specified coin. 
Throw CoinISNotPresentInSystemException if the coin is not found. Cached for efficiency.

3. /{coin}/info/in-period - retrieves the price details (oldest, newest, min, max) for the specified 
coin within a given date range (startDate and endDate). Throw CoinISNotPresentInSystemException if 
the coin is not found. Cached for efficiency.

4. /highest-normalized-range - identifies the coin with the highest normalized range 
for a specific day (date). Returns 404 if no data is available. Cached for efficiency.

5. /period-stats - provides statistics (oldest, newest, min, max) for a coin over the past
specified number of months (months). Defaults to 1 month if not provided.
Throw CoinISNotPresentInSystemException if the coin is not found. Cached for efficiency. 

To access endpoints detailed documentation and request/response entities description please use Swagger UI once the application is started and running - 
http://localhost:8080/swagger-ui/index.html \
\
To run the docker image of the service, please proceed to the location of the project inside your machine 
and run \
<mark>docker-compose --env-file .env up </mark>
\
Important Note - tests are disabled during docker image building because tests are using test containers,
which require docker. So, the situation like in that film with Leo DiCaprio - dream level inside dream level.

