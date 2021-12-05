import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.io.FileType

def extract(root, har) {
	def jsonSlurper = new JsonSlurper()

	File imageDir = new File("${root}/Text");
    if (! imageDir.exists()){
        imageDir.mkdir();
    }

    def toc;
    File tocFile = new File("${root}/toc.json")
    if(!tocFile.exists()) {
    	tocFile.createNewFile() 
    	toc = [:]
    } else {
    	toc = jsonSlurper.parseText(tocFile.text)
    }

	def jsonObj = jsonSlurper.parseText(new File("${root}/${har}").text)
	def index = 1;
	jsonObj.log.entries
	.findAll { it -> it.request.url.contains('xhtml') }
	.each { it ->
		def begin = it.request.url.lastIndexOf('/') + 1
		def end = it.request.url.indexOf('.xhtml')
		def name = it.request.url.substring(begin, end)
		def file = new File("${root}/Text/${name}.html")
		if(!file.exists()) {
			file << it.response.content.text
		} 


		if (!toc.any {tocValue -> tocValue.value.equalsIgnoreCase(name)}) {
			toc["${it.startedDateTime}"] = name	
		}
	}

	tocFile.write(JsonOutput.prettyPrint(JsonOutput.toJson(toc)))
}

def extractBase64(root) {
	def dir = new File("${root}/Text")
	dir.eachFile(FileType.FILES) {
		if (it.name.endsWith('html')) {
			def begin = it.text.indexOf('(self,')
			def end = it.text.indexOf('\')</script>')
			def content = it.text.substring((begin+7), end)
			new File("${root}/Text/${it.name}.base64") << content
		}
	}
}

def decodeBase64(root) {
	new File("${root}/Text")
	.listFiles({d, name -> name ==~ /.*.html.base64/ } as FilenameFilter)
	.sort { it.name }
	.each { it ->
		new File("${root}/Text/${it.name}.decoded") << new String(it.text.decodeBase64())
	}
}

def joinContent(root) {
	def content = ''
	def toc = new JsonSlurper().parseText(new File("${root}/toc.json").text)
	toc.sort { it.key }
	.each {
		println(it.value)
		content += new File("${root}/Text/${it.value}.html.base64.decoded").text
	}
	content = content.replaceAll('<body[^>]*>', '').replaceAll('</body[^>]*>', '')
	content = '<html><body>' + content + '</body></html>'
	new File("${root}/Text/all.xhtml") << content
	
}

def cleanup(root) {
	def deletedList = []
	new File("${root}/Text")
	.listFiles({d, name -> (name.endsWith('.html') || name.endsWith('.html.base64') || name.endsWith('.html.base64.decoded')) } as FilenameFilter)
	.each { it ->
		deletedList.add(it.name)
	}	

	deletedList.each { it -> 
		boolean fileSuccessfullyDeleted =  new File("${root}/Text/${it}").delete()
		println("deleted[${fileSuccessfullyDeleted}] ${root}/Text/${it}")
	}
}


def extractImage(root, har) {
	def jsonSlurper = new JsonSlurper()
	def jsonObj = jsonSlurper.parseText(new File("${root}/${har}").text)
	def index = 1;
	jsonObj.log.entries
	.findAll { entry -> entry.response.headers.any { header -> header.name.equalsIgnoreCase('content-type') && header.value.equalsIgnoreCase('image/jpeg') }}
	.each { entry -> 
		def begin = entry.request.url.lastIndexOf('/') + 1
		def end = entry.request.url.indexOf('.jpg')
		def name = entry.request.url.substring(begin, end)

		def dirName = "${root}/${dirName(entry.request.url)}"
		File imageDir = new File(dirName)
		if (! imageDir.exists()){
	        imageDir.mkdir();
	    }

		new File("${dirName}/${name}.jpg") << entry.response.content.text.decodeBase64()
	}
}

def dirName(url) {
	def last = url.lastIndexOf('/')
	def secondLast = url.substring(0, last).lastIndexOf('/')
	return url.substring(secondLast+1, last)
}

def extractCss(root, har) {
	File styleDir = new File("${root}/Styles");
    if (! styleDir.exists()){
        styleDir.mkdir();
    }

	def jsonSlurper = new JsonSlurper()
	def jsonObj = jsonSlurper.parseText(new File("${root}/${har}").text)
	def index = 1;
	jsonObj.log.entries
	.findAll { entry -> entry.response.headers.any { header -> header.name.equalsIgnoreCase('content-type') && header.value.equalsIgnoreCase('text/css') }}
	.each { it -> 
		def begin = it.request.url.lastIndexOf('/') + 1
		def end = it.request.url.indexOf('.css')
		def name = it.request.url.substring(begin, end)
		new File("${root}/Styles/${name}.css") << it.response.content.text
	}
}


def root = './go-set-a-watchman'
def bookHar = 'go-set-a-watchman.har'
extractImage(root, bookHar)
extractCss(root, bookHar)
extract(root, bookHar)
extractBase64(root)
decodeBase64(root)
joinContent(root)
cleanup(root)
