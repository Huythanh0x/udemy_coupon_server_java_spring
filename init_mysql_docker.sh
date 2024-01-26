#PULL AND RUN MYSQL CONTAINER
sudo docker run --name training-coupon-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql:latest
sudo docker start training-coupon-mysql

#ACCESS TO DATABASE INSIDE MYSQL CONTAINER
#get into container terminal
sudo docker exec -it training-coupon-mysql /bin/bash
#access to database
mysql -u root -p
#insert database password
password

#INIT DATABASE
#then execute all commands inside `init_data.sql`
