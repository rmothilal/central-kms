# central-kms

The central key mananage service facilitates forensic logging functionality for central services

The following document represents the services, APIs and endpoints resposible for various logging functions

Contents:

- [Configuration](#configuration)
- [API](#api)
- [Socket](#socket)
- [Logging](#logging)

## Configuration

### Environment Variables

| Environment Variable | Description | Default value |
| -------------------- | ----------- | ------------- |
| `DB_HOST` | Network address or ip addres of database server | `localhost` |
| `DB_PORT` | Port of database server | `5432` |
| `DB_USER` | User to connect to database | `kms` |
| `DB_PASSWORD` | Password of database user | `kms` |

## API

For endpoint documentation, see the [API documentation](API.md)

## Socket

For socket documentation, see the [Socket documentation](Socket.md)

## Logging

Logs are sent to standard output by default
