var ACTUALSCRIPT = null;

function showInformation(script){
	setActualScript(script);
	var url = document.location.href;
	if(url.indexOf("execution-uri-overview") != -1){
		$("#tx-list").load("./get-execution-uri?script="+script+"&mode=naked", function(){
			showExecutionUriTabActions();
		});
	} else if(url.indexOf("script-overview") != -1){
		$("#tx-list").load("./get-script?script="+script+"&mode=naked");
		$('#tx-contextual-buttons').empty();
		$('#tx-contextual-buttons').append('<ol><li><a href="javascript:deleteScript();" class="tx-button tx-button-remove">Delete</a></li>'+
			'<li><a href="javascript:updateScript();" class="tx-button tx-button-create">Save</a></li>'+
			'<li><a href="javascript:executeScript();" class="tx-button tx-button-modify">Execute</a></li></ol>');
	}
}

function countNamedElements(name) {
	var cnt = 0;
	var o = document.getElementsByName(name);
	for(var i = 0 ; i < o.length; i++){
        cnt++;
	}

	return cnt;
}

function showExecutionUriTabActions() {
	$('#tx-contextual-buttons').empty();
	if(countNamedElements('executionUriCheckBox') > 0) {
		$('#tx-contextual-buttons').append('<ol><li><a href="javascript:deleteExecutionUris()" class="tx-button tx-button-remove">Delete</a></li>'+
		'<li><a href="javascript:showAddExecutionUriForm();" class="tx-button tx-button-create">Add</a></li></ol>');
	} else {
		$('#tx-contextual-buttons').append('<ol><li><a href="javascript:showAddExecutionUriForm();" class="tx-button tx-button-create">Add</a></li></ol>');
	}
}

function setActualScript(script){
	ACTUALSCRIPT = script;
}

function updateScript(){
	$("#updateform").submit();
}

function deleteScript(){
	$.ajax({
		type: "POST",
		url: "./delete",
		data: "script="+$('#scriptUri').val(),
		success: function(msg){
			$("#tx-list").empty();
			$('#tx-contextual-buttons').empty();
			$("#tx-result").load("./script-list?mode=naked");
		}
	});
}

function installScript(){
	$("#installform").submit();
}

function executeScript(){
	$.ajax({
		type: "GET",
		url: "./execute",
		data: "script="+$('#scriptUri').val(),
		success: function(e) {
			$('#scriptConsole').empty();
			$('#scriptConsole').append('<h4>Script Output</h4><pre>'
				+e+'</pre>');
			$('#scriptConsole').slideDown('slow');
		}
	});
}

function showAddExecutionUriForm(){
	$('#addExecutionUriForm').empty();
	$('#addExecutionUriForm').append('<form id="addExecutionUri"><input type="hidden" id="scriptUri" name="scriptUri" value="'+ACTUALSCRIPT+'"></input>'
    +'<label for="executionUri">Execution URI</label>  <input type="text" name="executionUri"></input> '
    +'<input type="button" value="add" onclick="javascript:addExecutionUri()"></input>  </form>');
	$('#addExecutionUriForm').slideDown('slow');
}

function deleteExecutionUris(){
	$(':checked').each(
		function() {
			$.ajax({
				type: "POST",
				url: "./delete-executionUri",
				data: "executionUri="+$(this).val()+"&scriptUri="+ACTUALSCRIPT,
				success: function(e) {
					$("#tx-list").load("./get-execution-uri?script="+ACTUALSCRIPT+"&mode=naked", function(){
						showExecutionUriTabActions();
					});
				}
			});
		}
	);
}

function addExecutionUri(){
	var serialized = $('#addExecutionUri').serialize();
	$.ajax({
		type: "POST",
		url: "./add-execution-uri",
		data: serialized,
		success: function(msg) {
			$('#addExecutionUriForm').slideUp('slow');
			$("#tx-list").load("./get-execution-uri?script="+$('#scriptUri').val()+"&mode=naked", function(){
				showExecutionUriTabActions();
			});
		}
	});
}

function fileChoiceSelected() {
	if(document.getElementById('fileButton').checked) {
		$('#choiceCell').empty();
		$('#choiceCell').append('<input type="radio" name="fileChoice" id="fileButton" value="file" checked="checked" onclick="fileChoiceSelected()" /> Upload a File '+
						'<input type="radio" name="fileChoice" id="textButton" value="text" onclick="fileChoiceSelected()"/> Enter Script');
		$('#nameRow').remove();
		$('#fileCell').empty();
		$('#fileCellLabel').empty();
		$('#fileCellLabel').append('<label for="scriptFile">Script File:</label>');
		$('#fileCell').append('<input type="file" name="scriptFile" />');
	} else if(document.getElementById('textButton').checked) {
		$('#choiceCell').empty();
		$('#choiceCell').append('<input type="radio" name="fileChoice" id="fileButton" value="file" onclick="fileChoiceSelected()" /> Upload a File '+
						'<input type="radio" name="fileChoice" id="textButton" value="text" checked="checked" onclick="fileChoiceSelected()"/> Enter Script');
		$('#fileRow').before('<tr id="nameRow"><td><label for="scriptName">Script Name:</label></td><td><input type="text" name="scriptName" /></td></tr>');
		$('#fileCellLabel').empty();
		$('#fileCellLabel').append('<label for="scriptFile">Script:</label>');
		$('#fileCell').empty();
		$('#fileCell').append('<textarea name="scriptCode" id="scriptCode" rows="10" style="font-size:12px; width:70%;"></textarea>');
	}
}