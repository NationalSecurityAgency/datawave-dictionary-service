# Datawave Data Dictionary - Version 2
  Data Dictionary Version 2 is an on-going project that aims to expand the original Data Dictionary microservice by using an updated tech-stack to incorporate features useful for analysis and provide quick functions to query data. Below provides detailed instructions how to install, run, and edit configurations within this project.

  ## Technologies and Frameworks
  - **[Vue.js (v3.4)](https://vuejs.org/) [& Vite](vitejs.dev)** - For building the Frontend Components and HMR
  - **[Quasar (v2.8)](https://quasar.dev/)** - For constructing the Main Table and Inserting Data
  - **[Node.js (v14+) & NPM](https://nodejs.org/en)** - Package and Dependency Manager
  - **[Java 11 & Spring Boot](https://spring.io/)** - Supply Data _via_ RESTful Methods
  - **[Apache Accumulo](https://accumulo.apache.org/)** - Data Storage for Visualization

  ## Configuration and Setup
  ### Using Quasar/CLI & NPX
  Below shows the steps to run Data Dictionary V2 using the Quasar CLI which can be used to make quick edits or frontend configuration changes to the Dictionary. This strategy pairs really well with [Vite](vitejs.dev) to hot reload the modules and the [Vue DevTools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd?hl=en) to transform browser JS/CSS/HTML to Vue components.
  1. To run Data Dictionary V2 using the Quasar CLI, first install all the necessary technologies above along with [NPX](https://github.com/npm/npx). NPX is NPM Package Runner to execute the Data Dictionary V2 project and host it on a local port. Any changes you make in the `/frontend` directory (with Vite) should be reflected.
  2. Then head to the `/datawave` project and run the following:
  ```
  # 1 - Ensures the on the main branch for each module.
  git checkout integration
  git submodule foreach 'git checkout main || :'

  # 2 - Builds Datawave and Microservices
  mvn -Pcompose -Dmicroservice-docker -Dquickstart-docker -Ddeploy -Dtar -Ddist clean install -DskipTests -Dmaven.build.cache.enabled=false
  ```
3. Now Spin up the Docker Containers for each Microservice by going into `/datawave/docker` then run `docker compose up -d` and ensure "data-dictionary-1" service is started. The current Docker container is hosted on Port 8643 which can be seen in the "docker-compose.yml" file.
4. Head to https://localhost:8643/dictionary/data/v2/#/ (given you have the Datawave credentials) which is the static page where Spring Hosts Data Dictionary V2 (You can replace V1 to see that as well). This is also the endpoint for GET requests for the V2.
5. However, we need to modify the frontend components. Do this by going into the `/frontend` directory and running `npx quasar dev` to make changes in 'dev' mode. This will pull up a 'mock' page built by NPM to make your frontend changes with Vite hot reloading whatever change you make. Errors will also potray on this page as well.
6. Any final changes made will be reflected for deploy once you build again and rebuild the dictionary docker image simply calling this maven command in the `/dictionary/service` directory:
```
mvn clean install -Pdocker -DskkipTests
```

***

### Configuring Versions
#### Spring Boot Controller Configurations
Spring Configuration can be found in `/service/src/main/datawave.microservice/dictionary/` under the DataDictionaryControllerV2.java and HTMLControllerV2.java classes. When changing these controllers ensure to rebuild the project and test these changes similar to the process above.

#### Vue Configurations with Services
The main Vue build can be found under `/pages/DataDictionary.vue` which is the main driver for how the Data get accessed to the tables in Vue.

All the logic and filter functions to be changed in the future can be found under `/functions/formatters.ts` which contain comments to these TypeScript functions to filter data or other logic.

 The main configuration for **project** build files are:

- `/package.json` / `package-lock.json` - This contains how the project is built and all plugins managed by NPM. It also contains the versions, icons, and scripts to run the project with NPX.
- `qusar.config.js` for how Quasar behaves when managing quasar plugins (and compatible) and all the modules needed for Quasar to run. This also includes Vite.

***

_For General Setup such as Chrome Credentials to Launch the Authorization Microservice, or any other Microservice Setup and Configuraitons, please see the [Datawave README](https://github.com/NationalSecurityAgency/datawave/blob/integration/docker/README.md#usage)._
