# TODO

- [] add id to the entries
- [] make integration less coupled with implementation details
- [x] create generators as mirrors of the domain
- [x] create properties for the domain
- [] how to require the domain everywhere? AKA the specs
- [x] names should not be empty
- [] generator of basis should have more cross polinization
- [] generator for entire-setup should have input for max elements for every step
- [] add type to accounts
- [x] integrity checkers
- [] test integrity checkers
- [x] hiccup server start
- [] based index for relational-budget
- [] generic CRUD
- [] input to handler of generic input more generous, perhaps the entire request with new key, use interceptors
- [] have last of each in the state always to use for validation, maybe
- [] improve the architecture for get-state instead of just the function, maybe interceptor
- [] persist data from atom
- [] create seed data
- [] separate essential persiste state from derived information in domain
- [] create auxiliary relations
- [x] be able to persist and recover Instants
- [] categories should have balances and attributions that changed them
- [] relations generation for dates
- [] money should be moved between categories
- [] create assignment entity that assignes to categories
- [] create money movement entity that moves money between categories
- [] integrity assignment, category has to exist
- [] improve state to view contract to allow sorting
- [] other-party be a thing itself that can have its own relation isntead of being just an attr in entry
