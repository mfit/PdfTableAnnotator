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
// flag whether to use jquery-ui resizeable for active td-cell
var _useResizable = true;

/**
 * mousehandler object
 * 
 * @see handlers.js
 */
var controlsMouseHandler;

/**
 * height of control panel neccessary to calculate position of table relative to
 * table-container
 */
var control_panel_height = 45;

/**
 * turn table into "interactive table" (set class, wrap with container, draw
 * controls and set event handlers etc.. )
 * 
 * @param table
 * @param destcontainer
 *            [optional]
 * @parma rootpos [optional] parnet.offset() value , placement
 */
function jsTablify(table, destcontainer, rootpos) {

	if (table == undefined)
		return;
	table = $(table);

	// retrieve table id , if any
	var table_id = $(table).attr('data-id');

	// use provided container or get the parent of the table
	if (destcontainer == undefined) {
		destcontainer = $(table).parent(); // ref to parent
	}

	// container for controls + table
	var container = $('<span class="table-container">');

	// setup table
	$(table).addClass('jstable');

	// store previous position of table
	if (rootpos == undefined)
		rootpos = $(table).parent().offset() || 0;

	// setup table container + controlpanels
	var bcontrol = $('<div class="contr-panel">');
	bcontrol.append('<h4 class="handle"><span class="ui-icon ui-icon-arrow-4"></span>...</h4>');
	var closelink = $('<a href="#" title="close" class="btn btn-mini">\
			<span class="ui-icon ui-icon-circle-close"></span></a>');
	closelink.click(function() {
		if (confirm("Close Table?")) {
			jsTableDestroy(table);
			// do update with timout
			setTimeout(function() {
				notifyTableSaved()
			}, 100);
			controlsMouseHandler.resetHandlers();
		}
		return false;
	});
	bcontrol.append(closelink);
	bcontrol
			.append('<div class="contr contr-main"></div>');
	bcontrol.append('<div style="clear:both">');
	container.append(bcontrol);
	container.append(table);
	container.draggable({
		handle : ".handle",
		start : function()  {
			// override draggable's behaviour to set position relative ..
			$(".table-container").css('position', 'absolute');
		},
		stop : function() {
			_testTableControlBoundary(table);
		}
	})
	$(destcontainer).append(container);

	// position the newly created container and table within
	container.css('left', rootpos.left);
	container.css('top', rootpos.top - control_panel_height); // subtract
																// header/controls
	table.css('left', 0);
	table.css('top', 0);
	_testTableControlBoundary(table);

	// main controls

	// save :
	var b;
	b = $('<div class="btn btn-mini"><a href="#">\
			<span class="ui-icon ui-icon-circle-check"></span>Save</a></div>');
	b.click(function(e) {
		e.preventDefault();
		submitTableSchema(exportTable(table), docid, pagen, function() {
			jsTableDestroy(table);
			notifyTableSaved()
		});
	});
	$(".contr-main", container).append(b);

	// delete :
	if (table_id != undefined) {
		b = $('<div class="btn btn-mini"><a href="#"><span class="ui-icon ui-icon-cancel"></span>Delete</a></div>');
		b.click(function(e) {
			e.preventDefault();
			if (confirm("Delete Table?")) {
				$.ajax({
					type : "DELETE",
					url : "REST/document/" + docid + "/page/" + pagen
							+ "/table/" + table_id,
					error : function() {
						alert("Error deleting table.");
					},
					success : function(data, status, jqxhr) {
						jsTableDestroy(table);
						notifyTableSaved();
					},
				});

			}

		});
		$(".contr-main", container).append(b);
	}

	// init 2nd menu+behavior
	buildTableEditControls($(".contr-main", container), table);

	// ready MouseControlMouseHandler
	controlsMouseHandler.resetHandlers();
	controlsMouseHandler.setMarkCellHandlers(selectCellsMouseUpAction());
	
}

function selectCellsMouseUpAction() {
	return function(tdlist) {
		$(".jstable td").resizable().resizable("destroy");
		$("td.active").resizable({
			alsoResize : "td.active"
		});
	}
}

// ------------------------------------------------------------------------------
//
// Export and convert
//
// ------------------------------------------------------------------------------

/**
 * 
 * mapping of table from dom into json struct starting from a dom element
 * (table) , a json object is produced, containing rows and cells (sizes)
 * (instead of using the dom-table-structure, for storage, the table-structure
 * is converted into a json object )
 * 
 * @param table
 *            dom-element (table) to be "exported" (convert into json structure)
 * @param scale_factor
 *            a scale factor to be taken into account when writing bboxes and
 *            positions
 * 
 * @returns list-of-trs
 */
function exportTable(table, scale_factor) {

	// Calculate column-start and row-start.
	updateTableGrid(table);

	if (scale_factor == undefined) {
		scale_factor = outscale; // use global scaling factor (@see gui.js)
	}

	var t_top = $(table).offset().top / scale_factor;
	var t_left = $(table).offset().left / scale_factor;
	var tstruct = new Object();
	tstruct.id = $(table).attr('data-id') || 0; // id , if any

	// add trs and tds
	tstruct.trs = [];
	$("tr", table).each(
			function(i, obj) {
				var tds = [];
				$(obj).children().each(
						function(i, td) {
							tdstr = new Object();

							var tddim = _getCellDims($(td));
							tdstr.visgrid = [
									(tddim.x / scale_factor).toFixed(2),
									(tddim.y / scale_factor).toFixed(2),
									((tddim.x + tddim.w) / scale_factor)
											.toFixed(2),
									((tddim.y + tddim.h) / scale_factor)
											.toFixed(2) ];

							tdstr.rowspan = $(td).attr('rowspan') || 1;
							tdstr.colspan = $(td).attr('colspan') || 1;
							tdstr.startrow = $(td).attr('data-gridy')
									|| undefined;
							tdstr.startcol = $(td).attr('data-gridx')
									|| undefined;
							tdstr.classes = [ $(td).attr('data-tag') ]
									|| undefined;
							tds.push(tdstr);
						});
				tstruct.trs.push(tds);
			});
	return tstruct;
}

/**
 * convert/unserialize table into dom
 * 
 * @param tabledata 
 *            datastruct for table
 * @param container
 *            where to append dom elements to
 * @param scale_factor
 *            scaling-factor
 */
function importTable(tabledata, container, options) {

	if (tabledata.trs == undefined || tabledata.trs.length < 1
			|| tabledata.trs[0].length < 1) {
		console.log("import recieved empty tabledata");
		return;
	}
	
	var scale_factor = 1.0, 
		border_size = 0.0;
	if (options == undefined || options.scale == undefined) {
		scale_factor = 1.0;
	} else {
		scale_factor = options.scale;
	}
	
	if (options != undefined && options.border != undefined) {
		border_size = options.border;
	} 

	var tc = $('<span class="static-table-container">');
	tc.css('top', tabledata.trs[0][0].visgrid[1] * scale_factor);
	
	// find suitable left value . sometimes first cell in first row is 
	// not defined / null-cell.
	var left = tabledata.trs[0][0].visgrid[0];
	if (left<=0 && tabledata.trs.length > 1) {
		left = tabledata.trs[1][0].visgrid[0];
	}
	tc.css('left', left * scale_factor);

	var tab = $('<table class="static">');
	tab.attr('data-id', tabledata['tn']);
	$.each(tabledata.trs, function(j, tds) {
		var tr = $("<tr>"), 
			trsize = tds.length;
		
		for (var k=0; k < trsize; k++) {
			var tdstr = tds[k];
			var td = $("<td>");
			var width = Math.round((tdstr.visgrid[2] - tdstr.visgrid[0]) * scale_factor);
			td.css('width', width);
			var height = Math.round((tdstr.visgrid[3] - tdstr.visgrid[1]) * scale_factor);
			if(j!=tabledata.trs.length - 1) height = height - border_size; // account for border size
			td.css('height', height);
			td.attr('rowspan', tdstr.rowspan);
			td.attr('colspan', tdstr.colspan);
			for (i in tdstr.classes) {
				td.addClass("tag-" + tdstr.classes[i]);

				// TODO : support for multiple classes per cell
				td.attr('data-tag', tdstr.classes[i])
			}
			tr.append(td);
		}
		tab.append(tr);
	});
	tc.append(tab);
	$(container).append(tc);
	
	return tab;
}

/**
 * calls webservice / submits the tabledata (with POST to doc/page/table)
 * 
 * @param table
 * @param docid
 * @param pagen
 * @param success_callback
 */
function submitTableSchema(table, docid, pagen, success_callback) {
	var doSave = function() {
		$.ajax({
			type : "POST",
			url : "REST/document/" + docid + "/page/" + pagen + "/table",
			data : JSON.stringify(table),
			error : function() {
				alert("Error storing table.");
			},
			success : function(data, status, jqxhr) {
				setTimeout(function() {
					success_callback() // Update screens after POST.
				}, 100); // Delay callback somewhat.
			},
		});
	}
	doSave();
}

/**
 * turn an activly edited table back to a normal / "static" one Remove JsTable
 * Controls and unbind handlers
 * 
 * @param table
 */
function jsTableDestroy(table) {
	table = $(table);
	if (_useResizable) {
		$("td", table).resizable().resizable("destroy");
	}
	table.parent().remove();
}

// ------------------------------------------------------------------------------
//
// Table Edit
//
// ------------------------------------------------------------------------------
/**
 * split cell horizontally - split previously merged cells 
 * 
 * @param td
 */
function horizSplit(td) {

	updateTableGrid(td.parents("table"));

	// if rowspan > 1 , subtract 1 of rowspan
	// ... and add td to "right" position
	var rowspan = _getRowspan(td);
	var colspan = _getColspan(td);
	if (rowspan > 1) {

		var alltrs = $(td).parents("tbody").children("tr");
		var trindex = parseInt($(td).attr('data-gridy')) + rowspan - 1; // where
																		// to
																		// add
																		// TD?

		var nexttr = $(alltrs[trindex]).children("td");
		var gridx = $(td).attr('data-gridx');
		var insertafter = $(nexttr[0]); // set to default, first td
		for ( var i = 0; i < nexttr.length; i++) { // loop to find suitable
													// gridindex..
			if ($(nexttr[i]).attr('data-gridx') >= gridx - 1) {
				insertafter = $(nexttr[i]);
				break;
			}
		}

		var newtd = $("<td>");
		newtd.css('height', insertafter.css('height')); // copy/set height (TODO
														// : needs improvement)
		newtd.attr('colspan', colspan); // set same colspan
		newtd.insertAfter(insertafter);

		$(td).attr('rowspan', rowspan - 1); // .. and reduce rowspan

		// height correction - TODO : improve, this is overly simple
		$(td).css('height', $(td).height() - insertafter.height());

	}

}

/**
 * split cell vertically -  split previously merged cells
 * 
 * @param td
 */
function vertSplit(td) {

	// if colspan > 1 , subtract 1
	// ... and add a td to the right
	var colspan = _getColspan(td);
	var rowspan = _getRowspan(td);
	if (colspan > 1) {
		$(td).attr('colspan', colspan - 1);
		var newtd = $("<td>");
		newtd.insertAfter(td);
	}
}

/**
 * merge selected cells to one
 * 
 * @param table
 */
function mergeSelected(table) {

	// extend region selection
	extendCellSelectionToRect(table);

	var selected_cells = $("td.active", $(table));
	var dims = _findPartialGridDimension(selected_cells);

	selected_cells.addClass('_merging');

	// find top-left cell
	var topleft = $("td.active[data-gridx='" + dims[1] + "'][data-gridy='"
			+ dims[0] + "']")

	// set row- and colspan to full grid size
	topleft.attr('rowspan', (dims[2] - dims[0] + 1));
	topleft.attr('colspan', (dims[3] - dims[1] + 1));

	// delete all selected cells except topleft
	topleft.removeClass('_merging');
	$("._merging").remove();
}

/**
 * add column to the right
 * 
 * @param table
 */
function addCol(table) {
	clearActiveTds();
	var reftd = $("td", $("tr", table).first()).last();
	var newwidth = _getCellDims(reftd).w;
	$("tr", table).each(function(i, obj) {
		var newtd = $("<td>");
		newtd.css('width', newwidth);
		$(obj).append(newtd);
	});
}

/**
 * add row at bottom
 * 
 * @param table
 */
function addRow(table) {
	clearActiveTds();
	var firsttr = $("tr", table).first();
	var gridn = 0;
	$("td", $("tr", table).first()).each(function(i, obj) {
		gridn += parseInt($(obj).attr('colspan') || 1);
	});
	var maxheight = _max_height_of_elements($("td", $("tr", table).last()));
	var newtr = $("<tr>");
	for ( var i = 0; i < gridn; i++) {
		newtr.append('<td>');
	}
	$("td", newtr).first().css('height', maxheight);
	table.append(newtr);
}

function _max_height_of_elements(els) {
	var maxheight = 5;
	$(els).each(function(i, obj) {
		// var h = $(obj).outerHeight(); // el's height
		var h = _getCellDims($(obj)).h; // el's height
		maxheight = h > maxheight ? h : maxheight; // keep max
	});
	return maxheight;
}

function addRowAtPos(table) {
	var start_tr = 0;
	var grid = updateTableGrid(table);
	var selected_cells = $("td.active", $(table));

	if (selected_cells.length > 0) {
		var res = _findPartialGridDimension(selected_cells);
		start_tr = res[0]; // insert before tr of first selecte cell
	}
	var trs = $("tr", $(table));

	// get x-size of grid by looking at data-gridx-to of the last
	// td element in the first (any) tr . add one, because counting starts
	// at zero . the value - is set by updateTableGrid()
	var gridx = parseInt(
			$("tbody", $(table)).children('tr').first()
				.children('td').last().attr('data-gridx-to')) + 1;
	
	var newtr = $("<tr>");
	for ( var i = 0; i < gridx; i++) {
		newtr.append("<td>");
	}

	var newheight = _max_height_of_elements($("td", $(trs[start_tr])));
	$("td", $(newtr)).css('height', newheight);

	$(trs[start_tr]).before(newtr);
}

// ------------------------------------------------------------------------------
//
// Events, selections and GUI-&Table Helpers
//
// ------------------------------------------------------------------------------

/**
 * helper method to determine the location the td-cells in the grid
 * 
 * sets the actual grid positions of each td-cell in the table writes true grid
 * position in data-gridx and data-gridy
 * 
 * TODO : fix this , so that the returned array "grid" means something / has
 * correct dimensions + values ?
 * 
 * @param table
 * @returns
 */
function updateTableGrid(table) {
	var tb = $(table).children('tbody').first();

	// determine grid dimensions + prepare grid
	// all values will be "undefined"
	var gridx = tb.children('tr').first().children('td').length;
	var gridy = tb.children('tr').length;
	var grid = Array(gridy);
	for ( var j = 0; j < gridy; j++) {
		grid[j] = Array(gridx);
	}

	// loop over tr's
	// for each row, skip index-positions that are occupied by
	// td's with rowspan from above
	// set skip-markers accordingly, if a td should have rowspan
	tb.children('tr').each(function(row, o) {

		var rowinfo = grid[row];
		var colindex = 0;

		// loop over td's in row
		$(o).children('td').each(function(col, td) {

			// find td's we have to skip
			while (rowinfo.shift() != undefined) {
				colindex++;
			}

			// set x/y
			$(td).attr('data-gridx', colindex);
			$(td).attr('data-gridy', row);

			$(td).attr('data-gridx-to', colindex + _getColspan(td) - 1);
			$(td).attr('data-gridy-to', row + _getRowspan(td) - 1);

			// if this table has rows, mark td's in rows below
			// to be skiped ...
			if (_getRowspan(td) > 1) {
				for ( var r = 1; r < _getRowspan(td); r++) {
					for ( var s = 0; s < _getColspan(td); s++) {
						grid[row + r][colindex + s] = 0;
					}
				}
			}
			// increase gridindex by colspan
			colindex += _getColspan(td);
		});
	});
	return grid;
}

/**
 * helper to get rowspan
 */
function _getRowspan(td) {
	var val = $(td).attr('rowspan');
	if (val == undefined)
		return 1;
	else
		return parseInt(val);
}

/**
 * helper to get colspan
 */
function _getColspan(td) {
	var val = $(td).attr('colspan');
	if (val == undefined)
		return 1;
	else
		return parseInt(val);
}

/**
 * helper to calculate correct dimensions of td
 * 
 * @param td
 * @return x,y,w,h
 */
function _getCellDims(td) {
	var def_border = 1; // should be zero, but
				// firefox wont return any border with ever, so default 1 
	var tdpos = $(td).offset();
	var widthval = $(td).outerWidth()
			- (parseInt($(td).css('border-width')) | def_border);
	var heightval = $(td).outerHeight()
			- (parseInt($(td).css('border-width')) | def_border);
	return {
		x : tdpos.left,
		y : tdpos.top,
		w : widthval,
		h : heightval
	}
}

/**
 * helper funciton to find all "affected cells" of a selected region (all cells
 * within the boundaries of the region)
 * 
 * @param table
 * @param selected_cells
 */
function extendCellSelectionToRect(table) {

	updateTableGrid(table);

	var selected_cells = $("td.active", $(table));
	do {

		var setlen = selected_cells.length;

		var dim = _findPartialGridDimension(selected_cells);

		var gridminy = dim[0];
		var gridminx = dim[1];
		var gridmaxy = dim[2];
		var gridmaxx = dim[3];

		// add all cells within boundaries to selected
		var alltds = $("td", $(table));
		for ( var i = 0; i < alltds.length; i++) {
			var cell = $(alltds[i]);
			if (((parseInt(cell.attr('data-gridx')) >= gridminx && parseInt(cell
					.attr('data-gridx')) <= gridmaxx) || (parseInt(cell
					.attr('data-gridx-to')) >= gridminx && parseInt(cell
					.attr('data-gridx-to')) <= gridmaxx))
					&& ((parseInt(cell.attr('data-gridy')) >= gridminy && parseInt(cell
							.attr('data-gridy')) <= gridmaxy) || (parseInt(cell
							.attr('data-gridy-to')) >= gridminy && parseInt(cell
							.attr('data-gridy-to')) <= gridmaxy))) {
				// part of region
				cell.addClass('active');
			}
		}

		selected_cells = $("td.active", $(table));
	} while (selected_cells.length > setlen);

}

/**
 * find boundaries / maxvalues of selected cells in grid
 * 
 * @param table
 * @param selected_cells
 */
function _findPartialGridDimension(selected_cells) {
	var setlen = selected_cells.length;
	var gridminx = 99999, gridmaxx = 0, gridminy = 99999, gridmaxy = 0;
	// find boundaries / maxvalues of selected cells in grid
	for ( var i = 0; i < selected_cells.length; i++) {
		var cell = $(selected_cells[i]);
		if (parseInt(cell.attr('data-gridx')) < gridminx)
			gridminx = parseInt(cell.attr('data-gridx'));
		if (parseInt(cell.attr('data-gridx-to')) > gridmaxx)
			gridmaxx = parseInt(cell.attr('data-gridx-to'));
		if (parseInt(cell.attr('data-gridy')) < gridminy)
			gridminy = parseInt(cell.attr('data-gridy'));
		if (parseInt(cell.attr('data-gridy-to')) > gridmaxy)
			gridmaxy = parseInt(cell.attr('data-gridy-to'));
	}
	return [ gridminy, gridminx, gridmaxy, gridmaxx ]
}

/**
 * tag one or more td-cells "tag" a td-cell - i.e. add a class that signifies a
 * tag to a tdcell first, remove existing tags
 * 
 * @param td
 * @param tagname
 */
function tagCell(tds, tagname) {

	$(tds).each(function(i, tdobj) {
		var td = $(tdobj);

		// look for existing tag-classes and remove if any
		var clstr = $(td).attr('class');
		if (clstr != undefined) {
			var cur_tag_classes = clstr.split("/s/").filter(function(a) {
				return a.substring(0, 4) == 'tag-';
			});
			$(cur_tag_classes).each(function(i, str) {
				$(td).removeClass(str);
			});
		}

		// set new tag
		$(td).addClass('tag-' + tagname);
		$(td).attr('data-tag', tagname);
	});

}

/**
 * create controls, attach (click)handlers for table operations
 * 
 * @param container
 *            parent-element for controls
 * @param tab
 *            table-element on which controls should operate
 */
function buildTableEditControls(container, tab) {

	var rowColElements = _buildMenu({
		title : 'Row/Col',
		items : [ {
			title : 'Add row',
			action : function() {
				addRowAtPos(tab);
				return false;
			}
		}, {
			title : 'Add column',
			action : function() {
				addCol(tab);
				return false;
			}
		}, {}, {
			title : 'Remove row',
			action : function() {
				$("td.active", tab).parent().remove();
				return false;
			}
		}, {
			title : 'Remove cell',
			action : function() {
				$("td.active", tab).remove();
				return false;
			}
		}, ]
	});
	$(container).append(rowColElements);

	var mergOpElemeents = _buildMenu({
		title : 'Merge',
		items : [ {
			title : 'Merge',
			action : function() {
				mergeSelected($("table.jstable").first());
				return false
			}
		}, {}, {
			title : 'hSplit',
			action : function() {
				horizSplit($("td.active", tab).first());
				return false
			}
		}, {
			title : 'vSplit',
			action : function() {
				vertSplit($("td.active", tab).first());
				return false
			}
		}, ]
	});
	$(container).append(mergOpElemeents);

	var men_element = _buildMenu({
		title : 'Tag',
		items : [ {
			title : 'Cell',
			action : function() {
				tagCell("table.jstable td.active", "cell");
				return false;
			}
		},
		{
			title : 'Header',
			action : function() {
				tagCell("table.jstable td.active", "header");
				return false;
			}
		},
		
		// More classes could be added here
		
		]
	});
	$(container).append(men_element);
}

function _buildMenu(struct) {
	var men = $('<ul class="dropdown-menu"></ul>')
	for ( var i in struct.items) {
		if (struct.items[i].title == undefined) {
			var item = $('<li class="divider"></li>');
		} else {
			struct.items[i].action;
			struct.items[i].title;
			var item = $('<li><a href="#">' + struct.items[i].title
					+ '</a></li>');
			item.click(struct.items[i].action);
		}

		men.append(item);
	}

	var pmen = $('<div class="dropdown"></div>');
	var pitem = $('<a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#">'
			+ struct.title + '<span class="caret"></span></a>')
	pmen.append(pitem);
	pmen.append(men);

	return pmen;

}

/**
 * 
 * Event Handlers
 * 
 * set global event listeners will deleagte to handler set depending on mode
 */
function jsTable_setControllHandlers() {

	// turn doubleclicked static table
	// into an editable table
	$('body').dblclick(function(ev) {
		if ($(ev.target).is("table.static td")) {
			if ($("table.jstable").length > 0) {
				alert("Cannot edit more than one table at the same time.");
			} else {
				var myTab = $(ev.target).parent().parent().parent()[0];
				$(myTab).removeClass('static');
				jsTablify(myTab, $("#pdfcontent"));
			}
		}
	});
}

function clearActiveTds() {
	if (_useResizable)
		$("td").resizable().resizable('destroy');

	// turn off all other td's , tr's first
	$("td.active").removeClass('active');
	$("td.partner").removeClass('partner');
	$("tr.active").removeClass('active');
}

/**
 * switch the panel postion from top to bottom if panel does not fit on screen
 * (or switch back to top if does fit)
 * 
 * @param table
 */
function _testTableControlBoundary(table) {

	if (table.offset().top < 80
			&& $("table").prevAll(".contr-panel").length > 0) {
		table.after($(".contr-panel", table.parent())); // switch order
		table.parent().css(
				'top',
				table.parent().offset().top
						+ parseInt($(".contr-panel").css('height'))); // adapt position

	} else if (table.offset().top > 80
			&& $("table").nextAll(".contr-panel").length > 0) {
		table.before($(".contr-panel", table.parent())); // switch order
		table.parent().css(
				'top',
				table.parent().offset().top
						- parseInt($(".contr-panel").css('height'))); // adapt position
	}

	if (table.parent().offset().top < -10) {
		table.parent().css('top', 0);
	}

}