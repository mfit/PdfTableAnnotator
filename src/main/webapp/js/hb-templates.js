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
var temp_document_meta = Handlebars.compile('<p class="meta datapanel">\
		{{filename}}<br/>\
		{{pages}} pages, {{tables_count}} tables</p>');

var temp_layer_switch = Handlebars.compile('<div title="{{hint}}">\
		<input type="checkbox" id="{{id}}"  name="lcb-name-{{name}}" \
		data-layer-name="{{name}}" class="layer-switch"/>\
		<label for="{{id}}">{{title}}</label></div>');


var temp_document_view = Handlebars.compile('<div class="input-group-horiz clearfix">\
		<div><label>Page</label>\
		<a title="Prev.page" href="#" data-skip-bwd="1" class="btn">&lt;&lt;</a>\
		<input type="text" id="pagen" title="Jump to page (page numbering begins at 0)"/>\
		<a title="Next page" href="#" data-skip-fwd="1" class="btn">&gt;&gt;</a>\
		</div>\
		<div class="hborder"></div>\
		<div><label>Scale</label><select title="Set zoom/scale" class="docscale"></div>\
		</div>');
		
	
var dropdown_list = Handlebars.compile('<div class="dropdown btn">\
		<a class="dropdown-toggle {{class}}" data-toggle="dropdown" href="#">\
		<span class="{{icon-class}}"></span>{{title}}\
		<span class="caret"></span></a>\
		<ul class="dropdown-menu {{class}}"></ul></div>');

var user_settings = Handlebars.compile('<div><form>\
		<label>Repository:\
		<input type="text" name="repo" value="{{repo}}"/></label>\
		<br/><label>User:\
		<input type="text" name="user_id" value="{{user_id}}"/></label>\
		<input type="submit" class="btn save" value="Ok"/>\
		</form></div>'); 


var icon_button = Handlebars.compile('<div class="btn" \
		title="{{title}}">\
		<a href="#"><span class="ui-icon {{icon}}"></span>\
		{{caption}}</a></div>');
