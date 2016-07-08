/**
 * app related scripts
 */
$(document).ready(function() {

	window.visualise = function(container, dotData, edgeColor, nodeColor){  
		var container = document.getElementById(container);
		var parsedData = vis.network.convertDot(dotData);
		var data = {
				nodes: parsedData.nodes,
				edges: parsedData.edges
		}

		console.log(data);

		var options = parsedData.options;

		options.nodes = {
				color: nodeColor
		}
		options.edges = {
				color: edgeColor
		}

		for(edge of data.edges)
		{
			var label = edge.label;
			if(label.includes("Present in"))
			{
				edge.color = 'blue';
				data.nodes[edge.to].color = 'lime';
			}
			if(label.includes("Absent in"))
			{
				edge.color = 'blue';
				data.nodes[edge.to].color = 'red';
			}
		}

		var network = new vis.Network(container, data, options);
		//network.fit();
	}  

	$("#resp-div").hide();
	$("#formula-div").hide();
	$("#exp-div").hide();
	$("#rename-div").hide();
	$("#inpval-div").hide();

	$("#ltsdisplay").hide();

	$("#mode").val("conservative").change();
	$( "#noneOption" ).prop( "checked", true);

	$('input[type=radio][name=option]').change(function() {
		if ($(this).is(':checked')) {
			var opt = ($(this).val());
			if(opt === 'renaming')
			{
				$("#rename-div").show();
				$("#inpval-div").show();
				$("#exp-div").hide();
			}
			else if(opt === 'hiding')
			{
				$("#exp-div").show();
				$("#inpval-div").show();
				$("#rename-div").hide();
			}
			else 
			{
				$("#exp-div").hide();
				$("#inpval-div").hide();
				$("#rename-div").hide();
			}
		}
	});

	$('#mode').change(function() {
		var mode = ($(this).val());
		$('input[type=text]').val(''); 
		$('input[type=checkbox]').val(''); 
		if(mode === 'property-and' || mode === 'property-implied') {
			$("#optionContent").hide();
			$("#formula-div").show();

		}
		else {
			$("#optionContent").show();
			$("#formula-div").hide();
		}
	});


	$("#vform").submit(function(event){

		//disable the default form submission
		event.preventDefault();

		//grab all form data  
		var formData = new FormData($(this)[0]);

		$("#resp-div").hide();
		$("#graphContainer").html("");
		$("#graphContainer").hide();
		$("#model1Container").html("");
		$("#model1Container").hide();
		$("#model2Container").html("");
		$("#model2Container").hide();
		$.ajax({
			url: 'http://localhost:8080/transformation/vbpmn/validate/bpmn',
			type: 'POST',
			data: formData,
			cache: false,
			beforeSend: function () {
				$("#loader").show();
				if($("#response").hasClass("alert alert-success"))
					$("#response").removeClass("alert alert-success");
				if($("#response").hasClass("alert alert-danger"))
					$("#response").removeClass("alert alert-danger");
				if($("#response").hasClass("alert alert-warning"))
					$("#response").removeClass("alert alert-warning");
			},
			contentType: false,
			processData: false,
			error: function (returnData) {
				$("#loader").hide();
				$("#resp-div").show();
				$("#response").text(returnData.responseText); 
				$("#response").addClass("alert alert-danger");
			},
			success: function (returnData) {
				var resp = returnData.trim();
				var status = resp.split('|');
				var ceContainer = 'graphContainer';
				var model1Container ='model1Container';
				var model2Container = 'model2Container';
				$("#loader").hide();
				$("#resp-div").show();
				$("#response").text(status[0]);
				console.log(status)
				if(status[0].trim().toUpperCase() === "TRUE") {
					$("#response").addClass("alert alert-success");
					$("#model1Container").show();
					$("#model2Container").show();
					$('label[for="graphContainer"]').hide();
					$("#ltsdisplay").hide();
					$('#ltscheckbox').change(function() {
						if($(this).prop('checked'))
						{
							$("#ltsdisplay").show();
							$("#model1Container").html("");
							$("#model2Container").html("");
							visualise(model1Container, status[1], 'saddlebrown', 'peachpuff');
							visualise(model2Container, status[2], 'saddlebrown', 'peachpuff');
						}
						else {
							$("#ltsdisplay").hide();
						}
							
					});
				}
				else if (status[0].trim().toUpperCase() === "FALSE") {
					$("#graphContainer").show();
					$("#model1Container").show();
					$("#model2Container").show();
					$('label[for="graphContainer"]').show();
					$("#ltsdisplay").hide();
					$('#ltscheckbox').change(function() {
						if($(this).prop('checked'))
						{
							$("#ltsdisplay").show();
							$("#model1Container").html("");
							$("#model2Container").html("");
							visualise(model1Container, status[1], 'saddlebrown', 'peachpuff');
							visualise(model2Container, status[2], 'saddlebrown', 'peachpuff');
						}
						else {
							$("#ltsdisplay").hide();
						}
							
					});
					visualise(ceContainer, status[3], 'black', 'lightgrey');
					$("#response").addClass("alert alert-danger");
				}
				else {
					$("#response").html("<br /><p><strong>ERROR</strong> Unable to process. Please contact the team</p><br /><p>"+resp+"</p>");
					$("#response").addClass("alert alert-warning");
					$("#ltsbtn").hide();
					$('label[for="graphContainer"]').hide();
					$('label[for="model1Container"]').hide();
					$('label[for="model2Container"]').hide();
				}
			}
		});

		//event.unbind();

		return false;
	});

});