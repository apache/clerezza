function Parameters() {

	this.parameterArray = new Array(0);
    var url   = window.location.search;
    if (url != "")
     {
      url = url.substring(1,url.length);                      
      splittedURL = url.split("&"); 
      for (i=0;i < splittedURL.length;i++)
       {
        temp = splittedURL[i].split("=");
        currentField = new field();
				currentField.name = temp[0];
				currentField.value = Parameters.readField(temp[1]);
				this.parameterArray.push(currentField);
       }
     }
     
}

Parameters.prototype.getField = function(name) {

	for (i = 0; i < this.parameterArray.length; i++) {
		if (this.parameterArray[i].name == name) {
			return this.parameterArray[i].value;
		}
	}
	return null;
}

Parameters.readField = function(raw) {
	if (raw == null) {
		return null;
	}
	var result = raw.replace(/\+/g," ");
	return unescape(result);
}

	 
function field() {
 	var name;
	var value;
}
	
	
	
	


   
	 	