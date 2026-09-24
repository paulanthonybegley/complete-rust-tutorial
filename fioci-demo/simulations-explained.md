# What is simulated, what is real

Simulated: the API response is a catalog record with `mode="simulated"` and a
generated timestamp — the front end still renders every field.

Real (when compose is up): the same response, but `mode="live-compose"` and the
probe went over an actual TCP socket to `host:port`.

Nothing fakes an HTTP 200. The API is real either way; only the backing store
is mocked on purpose — so the *concept* is what you practice, not Docker.
