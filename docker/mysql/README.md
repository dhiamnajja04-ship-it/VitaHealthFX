# VitaHealthFX MySQL Docker

Comptes de test:

- Admin: `admin11@vitahealth.com` / `admin123`
- Medecin: `doctor@vitahealth.test` / `doctor123`
- Patient: `patient@vitahealth.test` / `patient123`
- Patient 2: `patient2@vitahealth.test` / `patient123`

Commandes utiles:

```powershell
docker compose up -d
docker compose logs -f vitahealth-mysql
docker compose down
docker compose down -v
```

Le script `init/01_schema_seed.sql` s'execute seulement lors de la premiere creation du volume Docker.
Pour reinitialiser totalement la base de test, utilisez `docker compose down -v`, puis `docker compose up -d`.
