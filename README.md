# Marketplace Microservice Platform
Event-driven marketplace backend built with Spring Boot, Kafka , Redis and ELK stack

# Architecture overview

The system is built using microservice architecture and asyncronous communication via kafka 

![Image alt](https://github.com/NasirovUmid/Marketplace/blob/master/Marketplace-architecture-diagram.drawio.png)

## Services

|Service               |  Responsibilty
| - - - - - -  - - - - - - - - - - - - - - - - - - - - - - - - - - |
|auth-service          | authentication, user events               |
|user-service          | user profiles                             |
|catalog-service       | courses, tickets                          |
|booking-service       | course reservations                       |
|payment-service       | confirmation of booking ( simplified )    |
|notification-service  | email notifications                       |
|analytic-service      | kafka consumers , ELK (without Kibana)    |


## Booking & Payment Flow

1. User chooses a ticket ( frontend already should have list of AVAILABLE tickets of a certain catalog/course and probably it sends random available ticket binding it with userId )
2. booking-service receives request, creates in database booking despite status and redis set new variable with TTL 10 min (in redis cli "CONFIG SET notify-keyspace-events Ex" (without quotes) to automize expiration informing)
3. booking-service instantly sends request (through kafka) for payment to -> payment-service and -> catalog service to reserve it , it sends back to necessary topic (redis values deleted to prevent automated expired event) and booking-service sends request to catalog to take (change status SOLD)
4. payment status event also sent to notification-service which inform user about status of payment through email and stores details to its database


 ## Kafka events 
 - UserEvent ->(topics = "users" , user.created)
 - TicketEvent ->(topics = "ticket.fail","ticket.success","ticket.reserve" , the result of booking and payment)
 - PaymentEvent ->(topics = "paymentevent", request for payment, topics = "payment.1","payment.2", (ordinal number of status) result of payment)

 All inter-communication is asynchronous via kafka
 Synchronous communication is intentionally avoided to reduce coupling and improve resilience.

 ## Tech stack

 - Java 21
 - Spring Boot
 - Spring Kafka
 - Redis
 - PostgreSQL
 - Kafka
 - Elasticsearch + Logstash
 - Docker & Docker-compose


## How to run
docker compose up --build


## Notes 
This project uses simplified payment model.
Real payment providers replaced with mock logic to focus on event-driven architecture

Full end-to-end testing is a work in progress
End-to-end flow is partially tested; full integration testing is planned as a next step

In the next iteration I plan to add contract tests and use analytics data to verify cross-service consistency
