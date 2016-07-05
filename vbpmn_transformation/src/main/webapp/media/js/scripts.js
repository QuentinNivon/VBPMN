/**
 * app related scripts
 */
$(document).ready(function() {

	$("#resp-div").hide();
	$("#formula-div").hide();
	$("#exp-div").hide();
	$("#rename-div").hide();
	$("#inpval-div").hide();

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
				$("#loader").hide();
				$("#resp-div").show();
				$("#response").text(resp);
				console.log(status)
				if(status[0].trim().toUpperCase() === "TRUE") {
					$("#response").addClass("alert alert-success");
				}
				else if (status[0].trim().toUpperCase() === "FALSE") {
					$("#graphContainer").show();
					$("#response").html("FALSE <br />");
					var container = document.getElementById('graphContainer');
					var parsedData = vis.network.convertDot(status[1]);
					var data = {
							nodes: parsedData.nodes,
							edges: parsedData.edges
					}

					console.log(data);

					var options = parsedData.options;

					options.nodes = {
							color: 'd3d3d3'
					}
					options.edges = {
							color: 'black'
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
					$("#response").addClass("alert alert-danger");
				}
				else {
					$("#response").html("<br /><p><strong>ERROR</strong> Unable to process. Please contact the team</p><br /><p>"+resp+"</p>");
					$("#response").addClass("alert alert-warning");
				}
			}
		});

		//event.unbind();

		return false;
	});

});