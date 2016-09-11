#!/usr/bin/env groovy

@Grab('org.yaml:snakeyaml:1.17')
import groovy.json.JsonBuilder
import org.yaml.snakeyaml.Yaml

def cli = new CliBuilder(usage: 'yaml-to-json.groovy -[fh]').with {
  h longOpt: 'help', 'Show usage'
  f longOpt: 'file', 'Yaml filename for for convert to json', args: 1, required: true
  c longOpt: 'console', 'output to stdout'

  parse(args) ?: System.exit(1)
}

if (cli.h) {
  cli.usage()
  return
}

String yamlText
if (cli.f) {
  yamlText = new File(cli.f).text
}

def yaml = new Yaml()
def model = yaml.load(yamlText)

if (cli.c)
  println new JsonBuilder(model).toPrettyString()
