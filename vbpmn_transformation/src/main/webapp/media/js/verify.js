/**
 * app related scripts
 */
$(document).ready(function () {

    window.visualise = function (container, dotData, edgeColor, nodeColor) {
        var container = document.getElementById(container);
        var parsedData = vis.network.convertDot(dotData);
        var data = {
            nodes: parsedData.nodes,
            edges: parsedData.edges
        }

        //console.log(data);

        var options = parsedData.options;

        options.nodes = {
            color: nodeColor
        }
        options.edges = {
            color: edgeColor
        }

        for (edge of data.edges) {
            var label = edge.label;
            if (label.includes("Present in")) {
                edge.color = 'blue';
                data.nodes[edge.to].color = 'lime';
            }
            if (label.includes("Absent in")) {
                edge.color = 'blue';
                data.nodes[edge.to].color = 'red';
            }
        }

        var network = new vis.Network(container, data, options);

        return network;
    }

    $("#resp-div").hide();
    //$("#formula-div").hide();
    $("#hide-rename-div").hide();

    $("#ltsdisplay").hide();

    //	$("#mode").val("conservative").change();
    //	$( "#noneOption" ).prop( "checked", true);

    var status = null;
    var model1 = null;
    //var model2 = null;
    var counterExample = null;

    $("#vform").submit(function (event) {

        //disable the default form submission
        event.preventDefault();

        //grab all form data  
        var formData = new FormData($(this)[0]);

        $("#resp-div").hide();
        $("#graphContainer").html("");
        $("#graphContainer").hide();
        $("#model1Container").html("");
        $("#model1Container").hide();
        $.ajax({
            url: 'http://localhost:8080/transformation/vbpmn/validate/verify',
            type: 'POST',
            data: formData,
            cache: false,
            beforeSend: function () {
                $("#loader").show();
                if ($("#response").hasClass("alert alert-success"))
                    $("#response").removeClass("alert alert-success");
                if ($("#response").hasClass("alert alert-danger"))
                    $("#response").removeClass("alert alert-danger");
                if ($("#response").hasClass("alert alert-warning"))
                    $("#response").removeClass("alert alert-warning");
            },
            contentType: false,
            processData: false,
            error: function (returnData) {
                $("#loader").hide();
                $("#resp-div").show();
                $("#response").text(returnData.responseText);
                $("#ltsbtn").hide();
                $("#resetVis").hide();
                $('label[for="graphContainer"]').hide();
                $('label[for="model1Container"]').hide();
                $("#response").addClass("alert alert-danger");
            },
            success: function (returnData) {
                var resp = returnData.trim();
                status = resp.split('|');
                var ceContainer = 'graphContainer';
                $("#loader").hide();
                $("#resp-div").show();
                $("#response").text(status[0]);
                console.log(status)
                if (status[0].trim().toUpperCase() === "TRUE") {
                    $("#response").addClass("alert alert-success");
                    $("#model1Container").show();
                    $('label[for="graphContainer"]').hide();
                    $("#ltsdisplay").hide();
                    $("#resetVis").hide();
                }
                else if (status[0].trim().toUpperCase() === "FALSE") {
                    $("#graphContainer").show();
                    $("#model1Container").show();
                    $('label[for="graphContainer"]').show();
                    $("#ltsdisplay").hide();
                    $("#resetVis").show();
                    counterExample = visualise(ceContainer, status[3], 'black', 'lightgrey');
                    $("#response").addClass("alert alert-danger");
                }
                else {
                    $("#response").html("<br /><p><strong>ERROR</strong> Unable to process. Please contact the team</p><br /><p>" + resp + "</p>");
                    $("#response").addClass("alert alert-warning");
                    $("#ltsbtn").hide();
                    $("#resetVis").hide();
                    $('label[for="graphContainer"]').hide();
                    $('label[for="model2Container"]').hide();
                }
            }
        });

        $('#ltscheckbox').change(function () {
            var model1Container = 'model1Container';
            //var model2Container = 'model2Container';
            if ($(this).prop('checked')) {
                $("#ltsdisplay").show();
                $("#resetVis").show();
                $("#model1Container").html("");
                model1 = visualise(model1Container, status[1], 'saddlebrown', 'peachpuff');
                model2 = visualise(model2Container, status[2], 'saddlebrown', 'peachpuff');
            }
            else {
                $("#ltsdisplay").hide();
                if (status[0].trim().toUpperCase() === "TRUE") {
                    $("#resetVis").hide();
                }
            }

        });

        //event.unbind();
        $('#resetVis').click(function () {
            if (null != counterExample)
                counterExample.fit();
            if (null != model1)
                model1.fit();
        });


        return false;
    });

    $("#tform").submit(function (event) {

        //disable the default form submission
        event.preventDefault();
    
        //grab all form data  
        var formData = new FormData($(this)[0]);
    
        $("#resp-div").hide();
        $("#graphContainer").html("");
        $("#graphContainer").hide();
        $("#model1Container").html("");
        $("#model1Container").hide();
        $.ajax({
            url: 'http://localhost:8080/transformation/vbpmn/transform/pif2bpmn',
            type: 'POST',
            data: formData,
            cache: false,
            beforeSend: function () {
                $("#loader").show();
                if ($("#response").hasClass("alert alert-success"))
                    $("#response").removeClass("alert alert-success");
                if ($("#response").hasClass("alert alert-danger"))
                    $("#response").removeClass("alert alert-danger");
                if ($("#response").hasClass("alert alert-warning"))
                    $("#response").removeClass("alert alert-warning");
            },
            contentType: false,
            processData: false,
            error: function (returnData) {
                $("#loader").hide();
                $("#resp-div").show();
                $("#response").text(returnData.responseText);
                $("#ltsbtn").hide();
                $("#resetVis").hide();
                $('label[for="graphContainer"]').hide();
                $('label[for="model1Container"]').hide();
                $("#response").addClass("alert alert-danger");
            },
            success: function (returnData) {
                var resp = returnData.trim();
                console.log(resp)
                status = resp.split('|');
                var ceContainer = 'graphContainer';
                $("#loader").hide();
                $("#resp-div").show();
                $("#response").text(status[0]);
                console.log(status)
                if (status[0].trim().toUpperCase() === "TRUE") {
                    $("#response").addClass("alert alert-success");
                    $("#model1Container").show();
                    $('label[for="graphContainer"]').hide();
                    $("#ltsdisplay").hide();
                    $("#resetVis").hide();
                }
                else if (status[0].trim().toUpperCase() === "FALSE") {
                    $("#graphContainer").show();
                    $("#model1Container").show();
                    $('label[for="graphContainer"]').show();
                    $("#ltsdisplay").hide();
                    $("#resetVis").show();
                    counterExample = visualise(ceContainer, status[3], 'black', 'lightgrey');
                    $("#response").addClass("alert alert-danger");
                }
                else {
                    $("#response").html("<br /><p><strong>ERROR</strong> Unable to process. Please contact the team</p><br /><p>" + resp + "</p>");
                    $("#response").addClass("alert alert-warning");
                    $("#ltsbtn").hide();
                    $("#resetVis").hide();
                    $('label[for="graphContainer"]').hide();
                    $('label[for="model2Container"]').hide();
                }
            }
        });
    });
});
