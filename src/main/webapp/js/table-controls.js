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

// global vars for area/splitlines
var areax = 0;
var areay = 0;
var linex = 0;
var liney = 0;

/**
 * set mouse-event handlers for click-drag an outer table box , 
 * the first step of table annotation process
 * on release, drawAreaControls() is called
 */
function readyAreaDraw(parent_element, callback) {
	
	// add draw class to parent
	parent_element.addClass('draw');
	
	// add visual
	var reg_ar = $('<div class="region-rectangle">');
	reg_ar.css('border')
	parent_element.append(reg_ar);
	
	// mousemove
	parent_element.on("mousedown.areaDraw", function(data) {
		areax = data.pageX;
		areay = data.pageY;
		parent_element.on("mousemove.areaDraw", function(data) {
			_setArea(reg_ar, areax, areay, data.pageX, data.pageY);
		});
		return false;
	});
	
	// btn up
	parent_element.on("mouseup.areaDraw", function(data) {
		// trigger the draw event
		// TODO : should round region values ?
		var event = jQuery.Event( "areaDrawn" );
		event.region = [{x:areax, y:areay}, {x:data.pageX, y:data.pageY}];
		parent_element.trigger(event);
		
		// remove hanlders 
		parent_element.off("mousemove.areaDraw");
		parent_element.off("mousedown.areaDraw");
		parent_element.off("mouseup.areaDraw");

		// remove visual
		$(reg_ar, parent_element).remove();
		parent_element.removeClass('draw');	// remove parent draw state
	});
}

/**
 * test / demo for an auto-detection from region  (experimental)
 * @param where dom parent element
 * @param region [{x,y},{x,y}]
 */
function doAutoDetectFromRegion(where, region) {
	
	$('#pdfcontent-layer-autodetect').remove();
	
	// scale region
	var rectregion = [region[0].x, region[0].y, region[1].x, region[1].y];
	for ( var i = 0; i < 4; i++ ) rectregion[i]=Math.round(rectregion[i] / outscale);
	
	// query backend for table detection
	$.getJSON("/REST/document/"+docid+"/page/"+pagen+
			"/classifier/table_from_region?region="+JSON.stringify(rectregion),
			function(data) {
				console.log(data);
				var lay = $('<div>');
				lay.addClass('pdfcontent-layer')
					.attr('id', 'pdfcontent-layer-autodetect');
				where.append(lay);
				importTable(data, lay, {scale:outscale}); 
			});
	

}

function createTableControlsFromRegion(where, region) {
	
	// remove and add "area" container
	$("#area", where).remove();
	where.append('<div id="area"><div id="innerarea"></div></div>');
	_setArea($("#area"), region[0].x, region[0].y, region[1].x, region[1].y);

	// draw controls on "area" container
	drawAreaControls($("#area"));
}

/**
 * reset spawn-region by clickdrag
 * add a callbackAction for what to do with the region, 
 * 		the callback takes 2 args : parentcontainer and region
 * 			
 * @param enable whether to enable / disable area-drawing
 * @param callbackAction what to call after area has been drawn (mousebutton release)   
 */
function resetArea(enable, callbackAction) {
	var parent_element = $("#pdfcontent");
	parent_element.unbind("areaDrawn");
	
	$(".areacontrols", parent_element).remove();
	$("#area", parent_element).remove();
	
	if (enable) { 
		// turn on 
		readyAreaDraw(parent_element);
		if (callbackAction != undefined) {
			// calback when region is drawn
			parent_element.bind("areaDrawn", function(e) {
				var parent_element = $(this);
				callbackAction(parent_element, e.region)
			});
		}
	} else {
		// turn off
		$("#area", parent_element).hide();
		parent_element.off("mousedown.areaDraw");
		parent_element.off("mouseup.areaDraw");
		parent_element.removeClass('draw');
	}
}

function _getNumericSort() { return function(a,b) {return a < b ? -1 : 1; } }

/**
 * resize an element to the rectangle 
 * 	spawned by top-left and bottom-right coordinates
 * @param x1
 * @param y1
 * @param x2
 * @param y2
 */
function _setArea(el, x1, y1, x2, y2) {
	startx = x2 < x1 ? x2 : x1;
	starty = y2 < y1 ? y2 : y1;
	endx = x2 > x1 ? x2 : x1;
	endy = y2 > y1 ? y2 : y1;
	$(el).css('left', startx)
		.css('width', endx - startx)
		.css('top', starty)
		.css('height', endy - starty);
}






/**
 * draw controls (+set handlers ) for further row/col-split of a  boundary box
 * controls are drawn around the dom element that is passed as parameter
 * @param obj
 */
function drawAreaControls(obj) {
	
	// remove old controls
	$("#pdfcontent .areacontrols").remove();

	// prepare dimensions of area
	var left 		= parseInt($(obj).css('left'), 		10);
	var top 		= parseInt($(obj).css('top'), 		10);
	var width 		= parseInt($(obj).css('width'), 	10);
	var height 		= parseInt($(obj).css('height'), 	10);

	// right panel, contains "close" and "export/save"
	var panel = $('<div class="areacontrols buttons"/>');
	$(panel).css('left', left + width+2)
		.css('width', 50)
		.css('top', top - 50)
		.css('height', height + 50+2);
	
	var close 	= $('<a href="#" class="btn close">X</a>');
	var exp 	= $('<a href="#" class="btn btn-success done">OK</a>');
	
	$(close).on("click.areaDraw",function() { 
		resetArea(false);
		return false;
	}).css('z-index',100); // close should always be visible
	$(exp).on("click.areaDraw",function() {
		
		// convert to table 
		var table = convertGridToTable();
		var areapos = $("#area").position();
		resetArea(false);	// turn off area drawing ..
		$("#pdfcontent").append(table);
		jsTablify($(table), undefined, areapos);
		
		return false;
		
	});
	$(panel).append(exp).append(close);
	$("#pdfcontent").append(panel);

	// left panel , enable set grid line on mouseover/click
	var panel = $('<div class="areacontrols split"/>');
	$(panel).css('left', left - 50).css('top', $(obj).css('top')).css('width',
			50).css('height', $(obj).css('height'));
	$(panel).on("mouseover.areaDraw", function(data) { // enable grid line , set handler for
										// click
		var ln = $('<div class="line"></div>');
		var parent_y = parseInt($(panel).css('top'));
		$("#innerarea").append(ln);
		$(ln).css('width', "100%");
		$(this).on("mousemove.areaDraw",function(data) {
			$(ln).css('left', 0);
			$(ln).css('top', data.pageY - parent_y - 50 /* panel height */);
		});
		$(this).on("click.areaDraw",function(data) {
			var fixedline = $(ln).clone(false); // dont clone eventhandlers
			fixedline.attr('class', 'fixedline');
			$("#innerarea").append(fixedline);
			fixedline.on('click', function() { $(this).remove()}); // remove on click
		});
	});
	$(panel).on("mouseout.areaDraw", function() { // remove grid line
		$("#innerarea").off("click.areaDraw");
		$("#pdfcontent").off("mousemove.areaDraw");
		$(".line").remove();
	});
	$("#pdfcontent").append(panel);

	// top panel , enable set grid line on mouseover/click
	var panel = $('<div class="areacontrols split"/>');
	$(panel).css('left', left).css('top', top - 50).css('width', width+2).css('height', 50);
	$("#pdfcontent").append(panel);
	$(panel).on("mouseover.areaDraw",function(data) { 
		// enable grid line , set handler for click
		var ln = $('<div class="line-vert"></div>');
		var parent_x = parseInt($(panel).css('left'));
		$("#innerarea").append(ln);
		$(ln).css('height', "100%");
		$(this).on("mousemove.areaDraw", function(data) {
			$(ln).css('left', data.pageX - parent_x);
			$(ln).css('top', 0);
		});
		$(this).on("click.areaDraw", function(data) {
			var fixedline = $(ln).clone(false); // dont clone eventhandlers
			fixedline.attr('class', 'fixedline-vert');
			fixedline.on('click', function() { $(this).remove()}); // remove on click
			$("#innerarea").append(fixedline);
		});
	});
	$(panel).on("mouseout.areaDraw", function() { 
		// remove grid line
		$("#innerarea").off("click.areaDraw");
		$("#pdfcontent").off("mousemove");
		$(".line-vert").remove();
	});
}




/**
 * convert the grid to a table to be further used for resizing/editions, 
 * tagging 
 * @returns table dom-element
 */
function convertGridToTable() {
	xstep = []
	ystep = []
	
	// add outer boundaries to array, element 0 is top resp. left property
	xstep.push(parseInt($("#area").css('left'), 10));
	xstep.push( xstep[0] + parseInt($("#area").css('width'), 10));
	ystep.push(parseInt($("#area").css('top'), 10));
	ystep.push( ystep[0] + parseInt($("#area").css('height'), 10));
	
	// loop over split-lines and add to array
	$("#innerarea .fixedline").each(function(index, data) {
		var v = parseInt($(data).css('top'), 10) + ystep[0];
		if (ystep.indexOf(v) < 0) ystep.push(v);	// add as split if not yet in list
	});
	$("#innerarea .fixedline-vert").each(function(index, data) {
		var v = parseInt($(data).position()['left']) + xstep[0];
		if (xstep.indexOf(v) < 0) xstep.push(v);	// add as split if not yet in list
	});

	// sort values and loop to create dom element for every cell
	xstep = xstep.sort(_getNumericSort());
	ystep = ystep.sort(_getNumericSort());
	var table = $('<table>');
	for ( var j = 0; j < ystep.length - 1; j++) {
		var tr = $('<tr>');
		for ( var i = 0; i < xstep.length - 1; i++) {
			var cell = $('<td>');
			$(cell)
				//.css('left', xstep[i])
				.css('width', xstep[i + 1] - xstep[i] - 1)	// subtract 1 for line width
				;
			$(cell)
				//.css('top', ystep[j])
				.css('height', ystep[j + 1] - ystep[j] -1) // 
				;
			
			$(tr).append(cell); // add
		}
		$(table).append(tr);
	}
	
	return table;
}

