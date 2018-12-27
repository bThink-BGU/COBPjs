# ContextBPjs
1) Install PostgreSQL

2) In shell access postgre:
psql -q -U postgres

3) Change the password of postgres user:
ALTER USER postgres PASSWORD 'postgres';

4) Create contexbpjs database:
create database contextbpjs;
GRANT ALL PRIVILEGES ON DATABASE contextbpjs to postgres;

5) Exite postgre shell
\q
