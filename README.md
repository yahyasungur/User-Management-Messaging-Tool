# User-Management-Messaging-Tool

Client-Server model

Server has a default list of admin users maintained in an RDBMS (say, PostgreSQL).

Admin vs Regular users.

Admins can perform CreateReadUpdateDelete (CRUD) operations on users.

Users have a number of fields: name, surname, birthdate, gender, e-mail address.

Users can send messages to each other

Each user can read their messages (both Inbox & Outbox)

Username & password authentication

Command Line Interface for Clients
