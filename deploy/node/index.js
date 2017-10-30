'use strict'

const Aws = require('./aws')
const Ecr = require('./ecr')
const Ecs = require('./ecs')
const Jfrog = require('./jfrog')
const Variables = require('./variables')

const pushService = ({IMAGE, NAME, PORT}, version) => {
  const envVariables = [
    {
      name: 'DB_HOST', 
      value: Variables.POSTGRES_HOST
    },
    {
      name: 'DB_USER',
      value: Variables.POSTGRES_USER
    }, 
    {
      name: 'DB_PASSWORD', 
      value: Variables.POSTGRES_PASSWORD
    }
  ]
  const serviceName = `${NAME}-${Variables.ENVIRONMENT}`
  return Ecr.pushImageToEcr(IMAGE, version)
    .then(result => Ecs.registerTaskDefinition(serviceName, result.versioned, PORT, envVariables))
    .then(taskDefinition => Ecs.deployService(Variables.CLUSTER, serviceName, taskDefinition))
}

const deploy = () => {
  const version = Variables.VERSION
  Aws.configureAws()
    .then(() => pushService(Variables.API, version))
    .then(() => Jfrog.login())
    .then(() => Jfrog.pushImageToJFrog(Variables.API.IMAGE, version))
    .catch(e => {
      console.error(e)
      process.exit(1)
    })
}

module.exports = deploy()
