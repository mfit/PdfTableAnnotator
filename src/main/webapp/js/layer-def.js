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
/*
 * Instantiate the object to manage layers
 */
var layerSetup = new contentLayers();

/**
 * Definitions and setup ( service resource and JS to creat Dom-Elements) of
 * visual information "layers" availaböe in the application. 
 */

// layer for the pdf through pdf.js
layerSetup.registerLayer("pdf",
		new contentLayer("pdf",
			function(params) { 
				$("#pdfcontent-layer-pdf", this.parent).remove();
				var canvas = $("<canvas>");
				canvas.attr('id', 'pdfcontent-layer-pdf')
					.addClass('pdfcontent-layer');
				
				// positioning needed to avoid influence on/by jstable-element:
				canvas.css('position', 'absolute').css('top', '0px');
				
				this.parent.append(canvas);
				PdfJsDisplayPage("pdfcontent-layer-pdf", "/REST/document/"+params.docid+"/pdf", (params.page+1), params.scale);
				return false;
			},
			"", {pri:1, caption: 'Pdf.js'})
);

//layer for annotated tables
layerSetup.registerLayer("tables", 
	new contentLayer("tables",
		function(params) {return ["REST","document",params.docid,"page",params.page,"tables"].join("/");},
		function(data) {
			data = JSON.parse(data);
			var container = $("<div>");
			$.each(data, function (i, tabledata) {
				importTable(tabledata, container , {scale:outscale});
			});
			return container;
		},
		{pri:99, caption: 'Tables'})
);

// layer for word-blocks
var wordlayer = new contentLayer("words", 
		function(params) {return ["REST","document",params.docid,"page",params.page,"layer","words"].join("/");},
		function(data) {
			var container = $("<div>");
			$.each(data, function (key, val) {
				word_div = $("<div>").addClass('bl')
					.css('left', val.x * outscale)
				 	.css('top', val.y * outscale - val.h * outscale)
				 	.css('width', val.w * outscale)
				 	.css('height', val.h * outscale)
				 	.css('font-size',val.h * 1.3 * outscale)
				 	.html(val.text);
				 	;
				word_div.attr('title', 'X:' + val.y.toFixed(2) + ' W:' + val.h.toFixed(2) 
						 + 'Y:' + val.y.toFixed(2) + ' H:' + val.h.toFixed(2));
				container.append(word_div);
			});
			return container;
		},
		{pri:2, caption:"Words/Blocks"}
	);

//layer for single characters
var char_spans_layer = new contentLayer("characters",
		function(params) {return ["REST","document",params.docid,"page",params.page,"layer", "chars"].join("/");},
		function(data) {
			var container = $("<div>");
			$.each(data, function ( key, val) {
				 charEl = $("<span>").addClass('c')
				 	.css('left', val.x * outscale)
				 	.css('top', (val.y * outscale) - (val.yscale * outscale))
				 	//.css('font-size', val.h * outscale)
				 	.css('font-size', val.yscale * outscale)
				 	//.css('line-height', (val.yscale * outscale)+"px")
				 	.attr('title', val.x.toFixed(2) +"/" + val.y.toFixed(2) + " [" 
				 				+ (val.x * outscale).toFixed(2) +"/" + (val.y * outscale).toFixed(2) + "]")
				 	.html(val.c);
				 $(container).append(charEl);
			});
			return container;
		},{pri:2, caption:'Characters'});


// Layer for bounding boxes of annotated tables
// This layer works a little differently, as it reads from the html export, 
// picks out the data matching the current page and reads the bboxes from there.
layerSetup.registerLayer("tablebbox", 
	new contentLayer("tablebbox",
		function(params) {return ["REST","document",params.docid,"export","html"].join("/");},
		function(data) {
			//data = JSON.parse(data);
			var container = $("<div>");
			var exportdata = $("<div>"+data+"</div>");	 // need to provide container
			var colstr = 'rgba(0,123,0,0.4)';
			// loop over tables
			$("table", exportdata).each(function(i, tobj) {
				// if table is on current page, add bboxes to dom
				if ($(tobj).attr('data-page') == (pagen+1) ) {	// (+1 for 0/1 count-start)
					$("td", tobj).each(function(j, obj) {
						var minx, miny, maxx, maxy;
						var el = $(obj);
						var bbox = eval(el.attr('data-bbox'));
						if (bbox == null) {
							// 
						} else {
							var charbbox = $("<div  class=\"cellbbox\">");
							charbbox.css('left', minx * outscale)
								.css('width', (maxx-minx) * outscale)
								.css('top', (miny * outscale))
								.css('height', (maxy-miny) * outscale);
							charbbox.css('background', colstr).css('position','absolute');
							charbbox.attr('title', 
										el.html()
										+ ' - X:' + minx.toFixed(2) 
										+ ' W:' + (maxx-minx).toFixed(2) 
										+ ' Y:' + miny.toFixed(2) 
										+ ' H:' + (maxy-miny).toFixed(2)
										);
							container.append(charbbox);
						}
					}); // each td
				}
			}); // each table
			return container;
		},
		{pri:5, caption: 'Cell BBoxes'})
);

//layer for imported external tables
layerSetup.registerLayer("exttable", 
	new contentLayer("exttable",
		function(params) {return ["REST","document",params.docid,"page",params.page,"layer", "import"].join("/");},
		function(data) {
			// data = JSON.parse(data);
			var container = $("<div>");
			$.each(data, function (i, tabledata) {
				var tab = importTable(tabledata, container , {
					scale:outscale,
					border:1.0});
				$(tab).addClass('externally-imported');
			});
			return container;
		},
		{pri:99, caption: 'External tables'})
);


// layer showing output of "sparse line detection"
var sparse_lines_layer = new contentLayer("lines",
		function(params) {return ["REST","document",params.docid,"page",params.page,"layer", "table"].join("/");},
		function(data) {
			var container = $("<div>");
			$.each(data, function (key, val) {
				 lineEl = $("<div>").addClass('bl2')
				 	.css('left', val.x * outscale)
				 	.css('top', val.y * outscale)
				 	.css('width', val.w * outscale)
				 	.css('height', val.h * outscale)
				 	.css('font-size',val.h*0.95 * outscale)
				 	.html(val.text);
				 	;
				 	container.append(lineEl);
			});
			return container;
		}, {pri:2, caption: 'SparseLines'});

layerSetup.registerLayer("lines", sparse_lines_layer);
layerSetup.registerLayer("characters", char_spans_layer);
layerSetup.registerLayer("words", wordlayer);
