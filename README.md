Spring boot server
============

The project is a Spring Boot server that crawls coupons from various websites and validates coupons by sending data to
the official Udemy API. It provides several API endpoints, including fetching coupons and searching
for coupons by query search. Authentication and authorization are included too.

### List source websites

- <https://jobs.e-next.in/course/udemy>
- <https://www.real.discount/>

### Techs, libs, frameworks
- Java 11
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring with Jwt](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Jpa](https://spring.io/projects/spring-data-jpa)
- [Mysql](https://hub.docker.com/_/mysql)

### Build from the source

#### Prerequisites:

- Java 11 or higher (Jdk)
- Docker to use mysql container

#### Steps to run from source:

1. Clone this repo
2. Pull and run mysql container with below command

```shell
sudo docker run --name training-coupon-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql:latest
sudo docker start training-coupon-mysql
```

3. Access to mysql container

```shell
sudo docker exec -it training-coupon-mysql /bin/bash
```

```shell
mysql -u root -p
```

```shell
password
```

3. Then paste entire SQL query from `init_data.sql` file to create database and tables.

4 (1) Start server with default profile

```shell
./gradlew bootRun
```

4 (2) Start server with dev profile

```shell
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The server will be started at <http://0.0.0.0:8080>

### API Endpoints

#### Coupon course response:

<details>
    <summary> Click to show hidden info</summary>
    <pre>
    {
        "lastFetchTime": "2023-07-12T15:32:01.748575966",
        "totalCoupon": 139,
        "totalPage": 14,
        "currentPage": 0,
        "courses": [
            {
                "courseId": 605750,
                "category": "Personal Development",
                "subCategory": "Unknown",
                "title": "Job Interview Skills Training Course | Successful Interviews",
                "contentLength": 73,
                "level": "All Levels",
                "author": "Mauricio Rubio - Agile Guru & Founder of AgileKB | AgileLee & Ureducation",
                "rating": 4.19763,
                "reviews": 320,
                "students": 7776,
                "couponCode": "466E1CBD03B841998363",
                "previewImage": "https://img-b.udemycdn.com/course/750x422/605750_58ea_2.jpg",
                "couponUrl": "https://www.udemy.com/course/10-steps-for-a-successful-interview-get-the-job/?couponCode=466E1CBD03B841998363",
                "expiredDate": "2023-07-16 06:19:00+00:00",
                "usesRemaining": 260,
                "heading": "Job Interview Skills Training to hit the ground running | Interview like a PRO and achieve success in your interviews",
                "description": "A really long HTML code here",
                "previewVideo": "/course/605750/preview/?startPreviewId=24000252",
                "language": "English"
            }
        ]
    }
    </pre>
</details>

#### Fetch with param

```
GET /api/v1/coupons?pageIndex=3&numberPerPage=20
```

#### Fetch with default param (pageIndex=0 numberPerPage=10)

```
GET /api/v1/coupons
```

#### Get course details (courseId = 12345)

```
GET /api/v1/coupons/12345
```

#### Search by title (querySearch = xxxx)

```
GET /api/v1/coupons/search?querySearch=xxxx
```

#### Filter by category, rating, content length, level, language

```
GET /api/v1/coupons/filter?category=Business&rating=3.0&contentLength=120&level=All Levels&language=English
```

#### User authentication response and request

```json
{
  "username": "test_username",
  "password": "password"
}
```

#### Register new account

```
POST /api/v1/register
```

#### Login to existing account

```
POST /api/v1/login
```

#### Get fingerprint token

```
GET /api/v1/auth/fingerprint-token
```

#### Get access token

```
GET /api/v1/auth/access-token
```
