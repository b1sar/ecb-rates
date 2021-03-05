# ecb-rates

An API for exchange rates. All the data is extracted from ECB(Europian Central Bank) database and is updated once in 1, 30 and 90 days periodically. 
Extracted data is stored in database. To be noted, ECB data is not perfectly complete, thus sometimes there may be missing days, in such a case the api
is set to request missing data seperately.

### Endpoints

#### `GET` `/api/v1/latest`
params
- `at`
- `base`
- `symbols`
#### `GET` `/api/v1/historical`
params
- `from`
- `to`
- `base`
- `symbols`
