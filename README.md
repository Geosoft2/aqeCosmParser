aqeCosmParser
===============

Java program that grabs Air Quality Egg (AQE) data from cosm.com

It covers all AQE tagged with "munster egg" (region of Münster) and retreives one datapoint in five minutes via the cosm API.

You'll need to add the following external libraries to your classpath:

JSON.Simple https://code.google.com/p/json-simple/

Apache HttpComponents: http://hc.apache.org/

log4j: http://logging.apache.org/log4j/1.2/

JDBC4 Postgresql Driver: http://jdbc.postgresql.org/download.html

Furthermore you'll need a config.properties file with the following content in your project folder

    apikey=[your cosm api key]
    db_username=[name of your database user]
    db_password=[password for database user]
    db_url=[url to database, fomatted as: ' //hostname:port/database name ']
