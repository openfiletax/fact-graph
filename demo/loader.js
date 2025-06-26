import * as fg from './fg.js'

let factGraph

function loadDictionary(event) {
  const file = event.target.files[0]
  const reader = new FileReader()
  reader.onload = () => setGraph(reader.result)
  reader.readAsText(file)
}

function setGraph(text) {
  const factDictionary = fg.FactDictionaryFactory.importFromXml(text)
  factGraph = fg.GraphFactory.apply(factDictionary)

  const options = factGraph.paths().map(path => {
    return `<option value="${path}">${path}</option>`
  })
  document.querySelector('#facts').innerHTML = options

  setError()
  displayGraph()
}

function getFact(event) {
  event.preventDefault()
  const form = event.target
  const path = form['get-fact'].value

  let fact = factGraph.get(path)
  console.log(fact)
  document.querySelector('#fact-result').innerText = fact
}

function setFact(event) {
  event.preventDefault()
  const form = event.target
  const path = form['fact'].value
  const value = form['value'].value

  try {
    factGraph.set(path, value)
  } catch (e) {
    setError(e.message)
    throw e
  }

  factGraph.save()
  setError()
  displayGraph()
}

function displayGraph() {
  const json = factGraph.toJSON()
  const prettyJson = JSON.stringify(JSON.parse(json), null, 2) // I know
  document.querySelector('#graph').innerText = prettyJson
}

function setError(msg) {
  const errorDiv = document.querySelector('#error')
  if (!msg) {
    errorDiv.classList.add('hidden')
  } else {
    errorDiv.classList.remove('hidden')
    errorDiv.innerText = msg
  }
}

window.loadDictionary = loadDictionary
window.setFact = setFact
window.getFact = getFact
