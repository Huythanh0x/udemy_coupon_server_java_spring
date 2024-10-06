# API Documentation

## Coupon Courses

### Fetch Coupon Courses

#### Fetch with parameters

```
GET /api/v1/coupons?pageIndex=3&numberPerPage=20
```

#### Fetch with default parameters (pageIndex=0, numberPerPage=10)

```
GET /api/v1/coupons
```

### Get Course Details

```
GET /api/v1/coupons/{courseId}
```

Example: `GET /api/v1/coupons/12345`

### Search Courses

```
GET /api/v1/coupons/search?querySearch={searchTerm}
```

### Filter Courses

```
GET /api/v1/coupons/filter?category={category}&rating={rating}&contentLength={contentLength}&level={level}&language={language}
```

Example:
```
GET /api/v1/coupons/filter?category=Business&rating=3.0&contentLength=120&level=All Levels&language=English
```

## User Authentication

### Register New Account

```
POST /api/v1/register
```

### Login to Existing Account

```
POST /api/v1/login
```

### Get Fingerprint Token

```
GET /api/v1/auth/fingerprint-token
```

### Get Access Token

```
GET /api/v1/auth/access-token
```

## Request and Response Examples

### User Authentication Request

```json
{
  "username": "test_username",
  "password": "password"
}
```

### Coupon Course Response

```json
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
```