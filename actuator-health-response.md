# ✅ Actuator Health Endpoint Response

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 511430909952,
        "free": 20698329088,
        "threshold": 10485760,
        "path": "C:\\Users\\USER\\Desktop\\demo 2\\demo\\.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    },
    "ssl": {
      "status": "UP",
      "details": {
        "validChains": [],
        "invalidChains": []
      }
    }
  }
}
```

This shows:
- ✅ Application status: UP
- ✅ Database (H2): UP  
- ✅ Disk space: UP
- ✅ Ping: UP
- ✅ SSL: UP
