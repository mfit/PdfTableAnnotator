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
 * 
 * Mouse Handler : Methods / Objects helping with handling mouse events
 * 
 * initialise e.g. with :
 * 
 * 		handlerobject = new MouseControlMouseHandler($("body"))
 * 
 * @param element
 */
function MouseControlMouseHandler(element) {
	this.state_md=false;
	this.down_pos_event;
	this.drag_td;
	this.orig_pos;
	this.d_x = 0;
	this.d_y = 0;
	
	this.td_list = [];
	this.td_first;
	this.td_last;
	this.sub_binds = [];
	
	this.h_down = function() {};
	this.h_up 	= function() {};
	this.h_move = function() {};
	this.action_finished = function() {};
	
	var myObj = this;
	
	// set proxy handlers on element
	$(element).mousedown(function(ev) {
		myObj.handleDown(ev);
	});
	$(element).mouseup(function(ev) {
		myObj.handleUp(ev);
	});
	$(element).mousemove(function(ev) {
		myObj.handleMove(ev);
	});
}

/**
 * down handler
 * store some event info and delegate do currently set handler
 */
MouseControlMouseHandler.prototype.handleDown = function(ev) {
	
	// important for smooth event handling, otherwise default
	// behaviours get int the way:
	ev.preventDefault(); 	
	
	this.state_md = true;
	this.down_pos_event = ev;
	this.d_x = 0;
	this.d_y = 0;
	if($(ev.target).is("td")) {
		this.drag_td = ev.target;
	} else {
		this.drag_td = undefined;
	}
	
	this.h_down(ev);	// call current down handler

}

/**
 * up handler
 * store some event info and delegate do currently set handler
 */
MouseControlMouseHandler.prototype.handleUp = function(ev) {
	this.state_md = false;
	this.h_up(ev); // call current up handler
}

/**
 * move handler 
 * store some event info and delegate do currently set handler
 */
MouseControlMouseHandler.prototype.handleMove = function(ev) {
	if (this.state_md) {
		this.d_w = ev.screenX - this.down_pos_event.screenX;
		this.d_h = ev.screenY - this.down_pos_event.screenY;
	}
	this.h_move(ev);	// call current move handler
}

/**
 * unset all handlers
 */
MouseControlMouseHandler.prototype.resetHandlers = function() {
	this.h_move = function(){}
	this.h_down = function(){}
	this.h_up 	= function(){}
	this.action_finished = function() {}
}


// -----------------------------------------------------------------------------------------

/**
 * enable a set of handlers
 * this set of handlers collects all TDs of jstabel after a mousedown event
 * also, the TDs get a class "active"
 * @param finishCallback optional callback after action
 */
MouseControlMouseHandler.prototype.setMarkCellHandlers = function(finishCallback) {

	extend_selection = function() {
		// Mark cells of rectangular area set by first-clicked cell
		// and last cell the mouse entered.
		$("td", $(".jstable")).removeClass('active');
		$(td_first).addClass('active');
		$(td_last).addClass('active');
		extendCellSelectionToRect($(".jstable"));
	}
	
	td_list = [];	// start with fresh list
	td_first = undefined; 
	
	// helper function 
	reset_markers = function() {
		// list mark reset 
		td_list = [];	// start with fresh list
		$(".jstable td").removeClass('active'); // remove all active class attrib
	}
	
	// down actions
	this.h_down = function(ev) {
		if($(ev.target).is(".jstable td")) {
			
			if ( ! ev.ctrlKey && ! ev.shiftKey ) {
				// reset selection - only if control or shift key is not pressed 
				reset_markers();	
			}
			
			td_list.push(ev.target);
			td_first = ev.target;
			$(ev.target).addClass('active');
			
			$(".jstable td").bind('mouseenter', function (ev) {
				// Enter td 
				var index = td_list.indexOf(ev.target);
				if (index < 0) {
					// .. entered unmarked td
					td_list.push(ev.target);
					//$(ev.target).addClass('active');	
					td_last = ev.target;
					extend_selection();
					
				} else {
					// .. entered marked td
					td_list.splice(index, 1);
					//$(ev.target).removeClass('active');	
				}
				
			});
		
		}
	}
	
	// up actions
	this.h_up = function(ev) {
		if ( ev.shiftKey ) {
			// shift pressed, mark whole region
			extendCellSelectionToRect($(".jstable"));
			//extend_selection();
		}
		
		$(".jstable td").unbind('mouseenter');
		this.action_finished(td_list);
	}
	
	// optionally set callback for after finisch
	if (finishCallback != undefined) {
		this.action_finished = finishCallback;
	}
}


