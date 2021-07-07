# OpenVASP Java Client

This is a reference implementation of a Java client for the OpenVASP standard.

> Standardization is the process of implementing and developing technical standards based on the consensus of different parties that include
> firms, users, interest groups, standards organizations and governments.
>
> - Wikipedia

## What is OpenVASP?

**VASP** stands for *Virtual Asset Service Provider*.
OpenVASP is an open protocol to implement FATF's travel rule for virtual assets.

The protocol facilitates robust compliance for VASPs, solely based on a set of principles, regardless of jurisdiction or virtual asset and without membership or registration with a centralized third-party.

[Whitepaper](https://openvasp.org/wp-content/uploads/2020/09/OpenVasp_Whitepaper.pdf) | [Read more →](https://www.openvasp.org/)

> The current implementation only includes the Whitepaper specs.
> [OVIPs](https://github.com/OpenVASP/ovips) (OpenVASP Improvement Proposals) are currently not implemented, but they are in the roadmap.
> The OVIPs were under heavy changes since the inception of the Whitepaper.
> OVIPs were created at the end of current implementation.

## Reference architecture

![Reference architecture](OpenVASP-whitepaper.drawio.svg)

![Protocol Stack](OpenVASP-stack.drawio.svg)

### Main Components

- **OpenVASP Client**.
It is the main artifact that implements the OpenVASP the protocol.
It is a redistributable Java library.
Core of **OpenVASP Client** exchanges messages as described in the white paper using the Whisper/WAKU protocol.
- **OpenVASP Host**.
It is a companion demo application.
Its only purpose is to showcase the **OpenVASP Client** to customers.
That is, it serves as an example of how to use the **OpenVASP Client**.
It contains a support persistance layer, *e.g.*, banking application.
- **OpenVASP UI**.
Javascript web application (Angular.js) using the **OpenVASP Host**.

## Getting started

This is a Maven project.

### How to build

```sh
mvn compile
```

Compiled classes and generated source code will be under the `target/` folder.

```text
target/generated-sources/web3j/java/org/openvasp/client/contract/VASP.java
```

Generates a stub to use the contract.

## Remarks

- Client exists as an initial working implementation (packaged as a .NET library).
The `openvasp-csharp-client` repo is heavily outdated
<https://github.com/OpenVASP/openvasp-csharp-client>
- Determine tests coverage
- Use of <https://downloads.bouncycastle.org/fips-java/BC-FJA-UserGuide-1.0.2.pdf>?
- Smart Contracts have been developed for identity claims
- Originator/Beneficiary roles provided by java-host
- Ethereum server/geth (Golang) needed to provide Whisper & Smart Contracts
- Java org.web3j RPC-JSON client for Java-client to connect to Ethereum
- Generate stubs for contracts using web3j for java-client
  <http://docs.web3j.io/latest/command_line_tools/#generated-javakotlin-project-structure>
- Smart Contracts only used for VASPs registration
Crucial information from Smart Contract is the VASP public encryption key

## Contributing

Want to help build this project? Check out our [contributing documentation](CONTRIBUTING.md).

## License

This project is licensed under the terms of the MIT License (see the file [LICENSE](LICENSE)).
