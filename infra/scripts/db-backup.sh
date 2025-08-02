#!/bin/bash
DATE=$(date +%F-%H%M)
docker exec streaker-postgres pg_dump -U postgres > /backups/pg_backup_$DATE.sql
