
function getPatientInfo(param_viewer)
{

	var url= param_viewer.wadoUrl + "/getPatientInfo?studyUID=" + param_viewer.studyUID + "&showOnlyThis=false";
	//alert(url);

	try{	
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState == XMLHttpRequest.DONE) {
				console.log(xhr.responseText);
				//alert(xhr.responseText);
				document.getElementById('txtAreaOutput').value=xhr.responseText;

				//var _xmlDoc = $.parseXML( xhr.responseText );
    			//$xmlDoc = $( _xmlDoc );
				//loadTests();
			}
		}		
		xhr.open("POST", url, true);
		xhr.setRequestHeader('Authorization','Basic ' + param_viewer.authorization);
		xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded');	
					
		xhr.withCredentials = true;
		xhr.send(null);		
	}
	catch(err)	
	{
		alert(err.message);
	}
}		

