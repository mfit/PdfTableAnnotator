/**
 * Copyright 2014 Matthias Frey.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

/**
 * Screen/display options - doc, page and scale.
 */
var pagen = 0;
var outscale = 1.5; // Scale / zoom factor
var docid = 1;

/**
 * Store meta info about document.
 */
var docmeta = {};

/**
 * Set the current document id (and page-number), set filename as title,
 * and set the document meta data to the info-panel.
 */
function setDocument(did, pid) {
	docid = did;
	$.getJSON([ 'REST', 'document', docid, 'meta' ].join("/"), function(data) {
		docmeta = data;
		$("#header .panel .meta").replaceWith(temp_document_meta(docmeta));
		document.title = docmeta['filename'];
	});
	if (pid != undefined) {
		pagen = pid;
	} else {
		pagen = 0;
	}
	$("#pagen").val(pagen);
}

function retrieveMetaData() {
	$.getJSON([ 'REST', 'document', docid, 'meta' ].join("/"), function(data) {
		docmeta = data;
		$("#header .panel p.meta").replaceWith(temp_document_meta(docmeta));
	});
}

// ------------------------------------------------------------------------------------------------
/**
 * Build main-menu / right-sidebar controls (by subsequent calls to other
 * methods that build parts).
 */
function pdfAnnotateBuildControls() {

	// placeholder for doc' meta info
	$("#header .panel").append('<span class="datapanel meta">');

	$("#header .panel").append('<h5>Documents</h5>');
	addDocumentSelector();
	addPageSelector();

	$("#header .panel").append("<hr/>");
	$("#header .panel").append('<h5>Tables</h5>');
	var block = $("<div class='stacked'>");
	addTableEditMenu(block);
	$("#header .panel").append(block);
	addExportsMenu($("#header .panel"));

	$("#header .panel").append("<hr/>");
	addLayerSelector(layerSetup);

	addExtrasMenu($("#header .panel"));
}

function addSettingsMenu() {
	var drawaddbutton = $(icon_button({
		caption : "Settings",
		icon : "ui-icon-wrench",
		title : "Choose repository"
	}));

	drawaddbutton.click(function(e) {
		e.preventDefault();
		modalUserSettings();
	});
	$("#header .panel").append(drawaddbutton);
}

function addDebugClickPanel(where) {
	$(where).append('<span class="data datapanel"></span>');
	$(where).append("<hr/>");

	//
	// debug handler for output on click
	//
	$("#pdfcontent").click(
			function(ev) {
				// output position of click
				$(".data").html(
						'Scaled: ' + ev.pageX + '/' + ev.pageY + '' + ' Orig: '
								+ (ev.pageX / outscale).toFixed(2) + '/'
								+ (ev.pageY / outscale).toFixed(2));
				// cell info
				if ($(ev.target).is(".jstable td")
						|| $(ev.target).is(".static td")) {
					$(".data").html(
							$(".data").html() + ' Grid:'
									+ $(ev.target).attr('data-gridx') + '/'
									+ $(ev.target).attr('data-gridy') + '-'
									+ $(ev.target).attr('data-gridx-to') + '/'
									+ $(ev.target).attr('data-gridy-to'));
				}
			});
}

function loadDocumentSelector(el) {
	$.get('/REST/documents', function(data) {
		$.each(data, function(i, o) {
			var shorttext = String(o).substring(0, 16);
			var opt = $('<option value="' + i + '">(' + i + ') ' + shorttext
					+ '</option>');
			if (i == docid)
				opt.attr('selected', 1);
			el.append(opt);
		})
	});
}

function addDocumentSelector() {

	// load available documents and present as select-field
	var docselect = $("<select>");
	loadDocumentSelector(docselect);
	docselect.addClass('document-load');
	docselect.change(function() {
		// select a new document + rebuild
		setDocument($(this).val(), 0)
		rebuildLayers();

		// add as history location
		window.history.pushState({}, "", '?' + docid + '/' + pagen);

	});
	$("#header .panel").append(docselect);

}

function pageInc() {
	updateScreen(pagen + 1);
	window.history.pushState({}, "", '?' + docid + '/' + pagen);
}

function pageDec() {
	updateScreen(pagen - 1);
	window.history.pushState({}, "", '?' + docid + '/' + pagen);
}

function addPageSelector() {
	$("#header .panel").append(temp_document_view);
	$("#pagen").change(function() {
		pagen = parseInt($(this).val());
		updateScreen(pagen);
	});
	$("a[data-skip-fwd]").click(function() {
		pageInc();
		return false;
	});
	$("a[data-skip-bwd]").click(function() {
		pageDec();
		return false;
	});
	function buildScaleButton(init, select) {
		if (init == undefined)
			init = 1;
		var scales = Array(0.5, 0.75, 1, 1.25, 1.5, 2, 2.5);
		for ( var i in scales) {
			var opt = $('<option value="' + scales[i] + '">' + scales[i]
					+ '</option>');
			select.append(opt);
		}
		select.change(function() {
			// select a new document + rebuild
			outscale = $(this).val();
			rebuildLayers();
		});
		select.val(init);
	}
	buildScaleButton(outscale, $(".docscale"));

}
/**
 * add layer-switches (checkboxes) to toggle layer visibility
 */
function addLayerSelector(layerControls) {
	var layercontent = $('<div class="layers-content collapse">'),
		l = layerControls.layer_names.length;

	for ( var i = 0; i < l; i++) {
		var lname = layerControls.layer_names[i];
		var lswitch = $(temp_layer_switch({
			name : lname,
			id : 'cbid_' + lname,
			hint : lname,
			title : layerSetup.layers[lname].settings['caption'],
		}));
		if (layerControls.getVisibility(lname)) {
			$("input", lswitch).attr('checked', 'checked');
		}
		layercontent.append(lswitch);
	}

	$(".layer-switch", layercontent).change(
			function() {
				var lname = $(this).attr('data-layer-name'), visstat = $(this)
						.is(':checked');
				layerControls.setVisibility(lname, visstat);
				layerControls.refresh();
			});

	$("#header .panel").append('<h5 class="layers-toggle collapse-toggle"\
			data-toggle="collapse" data-target=".layers-content">\
			Layers <b class="caret"></b></h5>');
	$("#header .panel").append(layercontent);
}

/**
 * Extra menu.
 */
function addExtrasMenu(where) {
	var extracontent = $('<div class="extras-content collapse">');
	
	var drawaddbutton = $(icon_button({
		caption : "Settings",
		icon : "ui-icon-wrench",
		title : "Choose repository"
	}));

	drawaddbutton.click(function(e) {
		e.preventDefault();
		modalUserSettings();
	});
	$(extracontent).append(drawaddbutton);
	
	// link to upload
	$(extracontent).append('<a href="repo" target="_blank" class="btn">Upload</a>');

	// debug panel
	addDebugClickPanel(extracontent);

	$(where).append('<h5 class="extras-toggle collapse-toggle" data-toggle="collapse"\
			data-target=".extras-content">More...<b class="caret"></b></h5>');
	$(where).append(extracontent);

}

function addTableEditMenu(parent) {
	// -----------------------------------------------
	// button to add annotation table by grid drawing
	var drawaddbutton = $(icon_button({
		caption : "Add",
		icon : "ui-icon-pencil",
		title : "To add a table, click and draw a rectangle with your mouse"
	}));

	drawaddbutton.click(function(e) {
		e.preventDefault();
		resetArea(true, createTableControlsFromRegion);
	});
	parent.append(drawaddbutton);
	
	// var button2 = $(icon_button({caption:"Autodetect from region",
	// icon:"ui-icon-pencil",
	// title:"Draw table region with your mouse"}));
	// button2.click(function(e){
	// e.preventDefault();
	// resetArea(true, doAutoDetectFromRegion);
	// });
	// $("#header .panel").append(button2);

}

function addExportsMenu(parent) {
	// html display as modal overlay
	expload = $('<div class="btn" title="View annotation data (tables)">\
			<a href="#"><span class="ui-icon ui-icon-folder-open"></span>\
			View</a></div><br/>');

	expload.click(function(e) {
		e.preventDefault();
		$('#myModal').removeData('modal');
		$('#myModal').modal({
			remote : "/REST/document/" + docid + "/export/html"
		})

		// download option
		$(".download-action").click(
				function() {
					window.location.href = "/REST/document/" + docid
							+ "/export/html?disposition=attachment";
				});
	});

	parent.append(expload);

	parent.append(dropdown_list({
		'class' : 'exports-dropdown',
		'title' : 'Exports...',
		'icon-class' : 'ui-icon ui-icon-document'
	}));
	
	
	var save_export = $('<a class="btn">Save</a>');
	save_export.click(function(e){
		e.preventDefault(); $.get("/REST/document/"+docid+"/write", function(data) {
			alert("ok");});
	});
	parent.append(save_export);

	var exports = [ [ "Regions", "region" ], 
	                [ "Structure", "structure" ],
	                [ "Functional model", "functional" ],
	                [ "Csv", "csv" ],
	                ];
	
	for ( var i in exports) {
		var button = $('<li><a href="#" class="export-link" data-export="'
				+ exports[i][1] + '">' + exports[i][0]
				+ '</a></li>');
		$("#header .panel ul.exports-dropdown").append(button);
	}
	$("#header .panel .export-link").click(
			function(e) {
				e.preventDefault();
				var expname = $(this).attr('data-export');
				window.location.href = "/REST/document/" + docid + "/export/"
						+ expname + "?disposition=attachment";
			});

	var sourcedl = $('<li><a href="#">SourcePDF</a></li>');
	sourcedl.click(function(e) {
		e.preventDefault();
		window.location.href = "/REST/document/" + docid + "/pdf";
	});
	$("#header .panel ul.exports-dropdown").append(sourcedl);

}

/**
 * Draw a grid (for debugging purposes)
 */
function drawGrid() {
	var grid = $('<div class="mygrid">');
	grid.css('z-index', -1);
	$("body").append(grid);
	$("#pdfcontent").css('z-index', 5);
	$("#header").css('z-index', 10);

	var gridsize = 25 * outscale;

	for ( var i = 0; i < 30; i++) {
		for ( var j = 0; j < 30; j++) {
			var b = $('<div class="debugbox">');
			b.css('left', i * gridsize).css('width', gridsize);
			b.css('top', j * gridsize).css('height', gridsize);
			grid.append(b);
		}
	}
}


/**
 * Display modal overlay where repository (and user_id) can be chosen.
 */
function modalUserSettings() {
	var modal = $('#myModal');
	$.get("/REST/settings", function(data) {
		
		$('.modal-body', modal).text(""); // clear modal contents
		$('.modal-body', modal).append(user_settings(data));
		var form = $('form', modal);
		
		function doSaveSettings(e) {
			e.preventDefault();
			var post_data = {
					repo : $('[name="repo"]', form).val(),
					user_id : $('[name="user_id"]', form).val(),
				};
			$.post("/REST/settings", post_data, function(data) {
				// done - refresh various elements and close modal
				$(".document-load options").remove();
				loadDocumentSelector($(".document-load"));
				setDocument(1, 0);
				rebuildLayers();
				$('#myModal').modal('hide');
			});
		}
		form.on('submit', doSaveSettings);
	});
	modal.modal();
}

// ------------------------------------------------------------------------------------------------

/**
 * refresh dynamic content
 */
function updateScreen(p, document) {
	pagen = p;
	if (pagen < 0)
		pagen = 0;
	if (docmeta != undefined) {
		if (pagen >= docmeta.pages)
			pagen = docmeta.pages - 1;
	}
	rebuildLayers();
	$("#pagen").val(pagen);
}

/**
 * call to refresh the screen foreach activated data-layer, load contents and
 * display
 */
function rebuildLayers() {
	layerSetup.setAllDirty().refresh();
}

/**
 * called from table-controls.js refresh layers that change through changes in
 * table-annotations
 */
function notifyTableSaved() {
	// these are the layers we know we want to redraw
	layerSetup.setDirty("tablebbox").setDirty("tables").refresh();
	retrieveMetaData();
}

// -----------------------------------------------------------------------------------
//
// Wrappers for Pdf-Js
//

/**
 * display a page of a docuemnt taken from (slightly modiefied) PdfJs
 * HelloWorld-Example
 */

function PdfJsDisplayPage(containerName, documentUrl, page, scale) {

	//
	// Disable workers to avoid yet another cross-origin issue (workers need the
	// URL of
	// the script to be loaded, and dynamically loading a cross-origin script
	// does
	// not work)
	//
	PDFJS.disableWorker = true;

	//
	// Asynchronous download PDF as an ArrayBuffer
	//
	PDFJS.getDocument(documentUrl).then(function(pdf) {
		pdf.getPage(page).then(function(page) {
			var viewport = page.getViewport(scale);

			//
			// Prepare canvas using PDF page dimensions
			//
			var canvas = document.getElementById(containerName);
			var context = canvas.getContext('2d');
			canvas.height = viewport.height;
			canvas.width = viewport.width;
			//
			// Render PDF page into canvas context
			//
			page.render({
				canvasContext : context,
				viewport : viewport
			});
		});
	});
}

/**
 * emtpy canvas
 * 
 * @param containerName
 */
function ClearCanvas(containerName) {
	var canvas = document.getElementById(containerName);
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);
}