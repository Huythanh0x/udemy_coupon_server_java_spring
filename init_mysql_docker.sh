#PULL AND RUN MYSQL CONTAINER
sudo docker run --name training-coupon-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql:latest
sudo docker start training-coupon-mysql

# Wait for MySQL to initialize (you may need to adjust the sleep duration based on your system)
sleep 10

# Execute MySQL command directly
#sudo docker exec -i training-coupon-mysql mysql -u root -ppassword -e "$(cat init_data.sql)"
sudo docker exec -i training-coupon-mysql mysql -h 127.0.0.1 -P 3306 -u root -ppassword -e "$(cat init_data.sql)"