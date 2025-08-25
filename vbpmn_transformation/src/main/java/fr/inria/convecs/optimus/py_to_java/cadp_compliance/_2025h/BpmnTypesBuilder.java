package fr.inria.convecs.optimus.py_to_java.cadp_compliance._2025h;

import fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics.BpmnTypesBuilderGeneric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class BpmnTypesBuilder extends BpmnTypesBuilderGeneric
{
	private static final String BPMN_TYPES_FILENAME = "bpmntypes.lnt";

	private static final String BPMN_TYPES =
			"(* BPMN data types (FACS'16), necessary for encoding unbalanced workflows *)\n" +
			"(* AUTHOR: Gwen Salaun *)\n" +
			"\n" +
			"module bpmntypes(id) with get, <, == is\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- set of identifiers -----*)\n" +
			"\n" +
			"type IDS is !card 48000\n" +
			"   set of ID \n" +
			"   with ==, !=, inter, card, empty, member, insert, union, remove, minus\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- flow -----*)\n" +
			"\n" +
			"type FLOW is !card 100\n" +
			"   flow (ident, source, target: ID)\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- set of flows -----*)\n" +
			"\n" +
			"type FLOWS is !card 100\n" +
			"   set of FLOW\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- task -----*)\n" +
			"\n" +
			"type TASK is !card 100\n" +
			"   task (ident: ID, incf, outf: IDS)\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- set of tasks -----*)\n" +
			"\n" +
			"type TASKS is !card 100\n" +
			"   set of TASK\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- initial event -----*)\n" +
			"\n" +
			"type INITIAL is !card 100\n" +
			"   initial (ident, outf: ID)  (*----- several outgoing flows (?) -----*)\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- final event -----*)\n" +
			"\n" +
			"type FINAL is !card 100\n" +
			"   final (ident: ID, incf: IDS)  (*----- several incoming flows (?) -----*)\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- set of final events -----*)\n" +
			"\n" +
			"type FINALS is !card 100\n" +
			"   set of FINAL\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- type of gateway -----*)\n" +
			"\n" +
			"type GSORT is\n" +
			"   xor, and, or\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- gateway pattern -----*)\n" +
			"\n" +
			"type GPATTERN is\n" +
			"   split, merge\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- gateway -----*)\n" +
			"\n" +
			"type GATEWAY is !card 100\n" +
			"   gateway (ident: ID, pattern: GPATTERN, sort: GSORT, incf, outf: IDS)\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- set of gateways -----*)\n" +
			"\n" +
			"type GATEWAYS is !card 100\n" +
			"   set of GATEWAY\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- node -----*)\n" +
			"\n" +
			"type NODE is !card 100          (*----- could it be simpler ? -----*)\n" +
			"   i (initial: INITIAL),\n" +
			"   f (finals: FINALS),\n" +
			"   g (gateways: GATEWAYS),\n" +
			"   t (tasks: TASKS)\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- set of nodes -----*)\n" +
			"\n" +
			"type NODES is !card 100\n" +
			"   set of NODE\n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- bpmn process -----*)\n" +
			"(*----- not the most optimized encoding for traversals -----*)\n" +
			"\n" +
			"type BPROCESS is !card 100\n" +
			"   proc (name: ID, nodes: NODES, flows: FLOWS)  \n" +
			"end type\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function\n" +
			"   is_merge_possible (p: BPROCESS, activeflows: IDS, mergeid: ID): Bool\n" +
			"is\n" +
			"   var incf: IDS, active_merge: Nat, status: Bool in\n" +
			"      incf := find_incf (p, mergeid);\n" +
			"      active_merge := find_active_tokens (activeflows, incf);\n" +
			"      if (active_merge == 0) then\n" +
			"         status := False\n" +
			"      else\n" +
			"         status := True\n" +
			"      end if;\n" +
			"      return status\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function find_incf (p: BPROCESS, mergeid: ID): IDS is\n" +
			"   case p\n" +
			"      var name: ID, nodes: NODES, flows: FLOWS in\n" +
			"         proc (name, nodes, flows) -> return find_incf_nodes (nodes, mergeid)\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function find_incf_nodes (nodes: NODES, mergeid: ID): IDS is\n" +
			"   case nodes\n" +
			"      var\n" +
			"         gateways: GATEWAYS, initial: INITIAL, finals: FINALS, tasks: TASKS,\n" +
			"         tl: NODES\n" +
			"      in\n" +
			"        cons (g (gateways), tl) -> return find_incf_gateways (gateways,\n" +
			"                                                              mergeid)\n" +
			"      | cons (i (initial), tl)\n" +
			"      | cons (f (finals), tl)\n" +
			"      | cons (t (tasks), tl) -> return find_incf_nodes (tl, mergeid)\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function find_incf_gateways (gateways: GATEWAYS, mergeid: ID): IDS is\n" +
			"   case gateways\n" +
			"      var\n" +
			"         ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS,\n" +
			"         tl: GATEWAYS\n" +
			"      in\n" +
			"        cons (gateway (ident, pattern, sort, incf, outf), tl) ->\n" +
			"           if (ident == mergeid) then\n" +
			"              return incf\n" +
			"           else\n" +
			"              return find_incf_gateways (tl, mergeid)\n" +
			"           end if\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function find_active_tokens (activeflows:IDS, incf:IDS): Nat is\n" +
			"   var tokens: IDS, count: Nat in\n" +
			"      tokens := inter (activeflows, incf);\n" +
			"      count := card (tokens);\n" +
			"      return count\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*-------------------------------------------------------------------------*)\n" +
			"(*-----------------Check for merge with BPMN 1.x semantics-----------------*)\n" +
			"(*-------------------------------------------------------------------------*)\n" +
			"\n" +
			"function\n" +
			"   is_merge_possible_v2 (p: BPROCESS, activeflows:IDS, mergeid:ID): Bool\n" +
			"is\n" +
			"   var\n" +
			"      incf: IDS, inactiveincf: IDS, active_merge: Nat, visited: IDS,\n" +
			"      result1: Bool\n" +
			"   in\n" +
			"      visited := nil;\n" +
			"      (*----- just iterate through gateways instead of all nodes -----*)\n" +
			"      incf := find_incf (p, mergeid);      \n" +
			"      active_merge := find_active_tokens (activeflows, incf);\n" +
			"\n" +
			"      (*----- check if all the incf have tokens -----*)\n" +
			"      if (active_merge == card (incf)) then\n" +
			"         return True\n" +
			"      else\n" +
			"         (*----- first remove incf with active tokens -----*)\n" +
			"         inactiveincf := remove_ids_from_set (activeflows, incf);\n" +
			"\n" +
			"         (*----- then check upstream for remaining flows -----*)\n" +
			"         result1 := check_af_upstream (visited?, p, activeflows,\n" +
			"                                       inactiveincf);\n" +
			"         return result1\n" +
			"      end if\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function\n" +
			"   is_sync_done (p: BPROCESS, activeflows, syncstore: IDS, mergeid:ID): Bool\n" +
			"is \n" +
			"   var incf: IDS, activesync: IDS in\n" +
			"      (*----- just iterate through gateways instead of all nodes -----*)\n" +
			"      incf := find_incf (p, mergeid);          \n" +
			"      activesync := inter (activeflows, incf); \n" +
			"      if (empty (activesync)) then\n" +
			"         return False\n" +
			"      elsif (inter (activesync, syncstore) == activesync) then\n" +
			"         return True\n" +
			"      else\n" +
			"         return False\n" +
			"      end if\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Merge check for parallel gateways -----*)\n" +
			"\n" +
			"function\n" +
			"   is_merge_possible_par (p:BPROCESS, syncstore: IDS, mergeid:ID): Bool\n" +
			"is \n" +
			"   var incf, activesync: IDS in\n" +
			"      (*----- just iterate through gateways instead of all nodes -----*)\n" +
			"      incf := find_incf (p, mergeid);          \n" +
			"      if (inter (incf, syncstore) == incf) then\n" +
			"         return True\n" +
			"      else\n" +
			"         return False\n" +
			"      end if\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- finds all the upstream flows and checks for tokens -----*)\n" +
			"\n" +
			"function\n" +
			"   check_af_upstream(in out visited: IDS, p: BPROCESS, activeflows: IDS,\n" +
			"                     incf: IDS): Bool\n" +
			"is\n" +
			"   var count: Nat, result1: Bool, result2: Bool in\n" +
			"      case incf\n" +
			"         var hd: ID, tl: IDS, upflow: IDS, source: ID in\n" +
			"            cons(hd, tl) ->\n" +
			"               source := find_flow_source (p, hd);\n" +
			"\n" +
			"               if (source == DummyId) then\n" +
			"                  return True\n" +
			"               elsif (member (source, visited)) then\n" +
			"                  result1 := check_af_upstream (visited?, p, activeflows,\n" +
			"                                                tl);\n" +
			"                  return result1\n" +
			"               else\n" +
			"                  visited := insert (source, visited);\n" +
			"                  upflow := get_incf_by_id(p, source);\n" +
			"\n" +
			"                  if (upflow == nil) then\n" +
			"                     return True\n" +
			"                  end if;\n" +
			"\n" +
			"                  count := find_active_tokens (activeflows, upflow);\n" +
			"                  \n" +
			"                  if (count == 0 of Nat) then\n" +
			"                     result1 := check_af_upstream (visited?, p,\n" +
			"                                                   activeflows, upflow);\n" +
			"                     result2 := check_af_upstream (visited?, p,\n" +
			"                                                   activeflows, tl);\n" +
			"                     return result1 and result2\n" +
			"                  else\n" +
			"                     return False\n" +
			"                  end if\n" +
			"               end if\n" +
			"         | nil -> return True\n" +
			"      end case\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function find_flow_source (bpmn: BPROCESS, flowid: ID): ID is\n" +
			"   case bpmn\n" +
			"      var name: ID, nodes: NODES, flows: FLOWS in\n" +
			"         proc (name, nodes, flows) -> return traverse_flows (flows, flowid)\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function traverse_flows (flows: FLOWS, flowid:ID): ID is\n" +
			"   var dummySource: ID in \n" +
			"      dummySource := DummyId;\n" +
			"      case flows\n" +
			"         var ident: ID, source: ID, target: ID, tl: FLOWS in\n" +
			"            cons (flow (ident, source, target), tl) ->  \n" +
			"               if (ident == flowid) then\n" +
			"                  return source\n" +
			"               else\n" +
			"                  return traverse_flows (tl, flowid)\n" +
			"               end if\n" +
			"         | nil -> return dummySource\n" +
			"      end case\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- given a node id, gets its incoming flows -----*)\n" +
			"\n" +
			"function get_incf_by_id (p: BPROCESS, nodeid: ID): IDS is\n" +
			"   case p\n" +
			"      var name: ID, nodes: NODES, flows: FLOWS in\n" +
			"          proc (name, nodes, flows) -> return traverse_nodes (nodes, nodeid)\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Traverse across all nodes in search of the node -----*)\n" +
			"\n" +
			"function traverse_nodes (nodes: NODES, id: ID): IDS is\n" +
			"   case nodes\n" +
			"      var\n" +
			"         gateways: GATEWAYS, initial: INITIAL, finals: FINALS, tasks: TASKS,\n" +
			"         tl: NODES, incf:IDS\n" +
			"      in\n" +
			"         cons (g (gateways), tl) ->\n" +
			"            incf := traverse_gateways (gateways, id);\n" +
			"            if (nil == incf) then\n" +
			"               return traverse_nodes(tl, id)\n" +
			"            else\n" +
			"               return incf\n" +
			"            end if\n" +
			"      | cons (i (initial), tl) -> return traverse_nodes (tl, id)\n" +
			"      | cons (f (finals), tl) ->\n" +
			"           incf := traverse_finals(finals, id);\n" +
			"           if (nil == incf) then\n" +
			"              return traverse_nodes(tl, id)\n" +
			"           else\n" +
			"              return incf\n" +
			"           end if\n" +
			"      | cons (t (tasks), tl) ->\n" +
			"           incf := traverse_tasks (tasks, id);\n" +
			"           if (nil == incf) then\n" +
			"              return traverse_nodes (tl, id)\n" +
			"           else\n" +
			"              return incf\n" +
			"           end if\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Find incf of gateways -----*)\n" +
			"\n" +
			"function traverse_gateways (gateways: GATEWAYS, id: ID): IDS is\n" +
			"   case gateways\n" +
			"      var\n" +
			"         ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS,\n" +
			"         tl: GATEWAYS\n" +
			"      in\n" +
			"        cons (gateway (ident, pattern, sort, incf, outf), tl) ->\n" +
			"           if (ident == id) then\n" +
			"              return incf\n" +
			"			else\n" +
			"   		   return traverse_gateways (tl, id)\n" +
			"			end if\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Find incf of finals -----*)\n" +
			"\n" +
			"function traverse_finals (finals: FINALS, id: ID): IDS is\n" +
			"   case finals\n" +
			"      var ident: ID, incf: IDS, tl: FINALS in\n" +
			"        cons (final (ident, incf), tl) ->\n" +
			"           if (ident == id) then\n" +
			"   	       return incf\n" +
			"			else\n" +
			"   		   return traverse_finals (tl, id)\n" +
			"			end if\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Find incf of taks -----*)\n" +
			"\n" +
			"function traverse_tasks (tasks: TASKS, id: ID): IDS is\n" +
			"   case tasks\n" +
			"      var ident: ID, incf: IDS, outf: IDS, tl: TASKS in\n" +
			"        cons (task (ident, incf, outf), tl) ->\n" +
			"           if (ident == id) then\n" +
			"              return incf\n" +
			"           else\n" +
			"              return traverse_tasks(tl, id)\n" +
			"           end if\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Remove Incoming flows from activetokens -----*)\n" +
			"\n" +
			"function remove_incf (bpmn: BPROCESS, activeflows: IDS, mergeid: ID): IDS is\n" +
			"   var incf: IDS in\n" +
			"      incf := get_incf_by_id (bpmn, mergeid);\n" +
			"      return remove_ids_from_set (incf, activeflows)\n" +
			"   end var\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"function remove_sync (bpmn: BPROCESS, syncstore: IDS, mergeid: ID): IDS is\n" +
			"   return remove_incf (bpmn, syncstore, mergeid)\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Helper functions to remove a set of IDS from the set ----- *)\n" +
			"\n" +
			"function remove_ids_from_set (toremove:IDS, inputset: IDS): IDS is\n" +
			"   return minus (inputset, toremove) \n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*--------------------------------------------------------------------------*)\n" +
			"(*--------------------------------------------------------------------------*)\n" +
			"(*--------------------------------------------------------------------------*)\n" +
			"(*------------Another version of code for process node traversal------------*)\n" +
			"(*------------------Fix: Remove the code from final version-----------------*)\n" +
			"(*--------------------------------------------------------------------------*)\n" +
			"(*--------------------------------------------------------------------------*)\n" +
			"(*--------------------------------------------------------------------------*)\n" +
			"\n" +
			"(*----- Traverse across all nodes in search of the node -----*)\n" +
			"\n" +
			"function find_incf_nodes_all (nodes: NODES, id: ID): IDS is\n" +
			"   case nodes\n" +
			"      var\n" +
			"         gateways: GATEWAYS, initial: INITIAL, finals: FINALS, tasks: TASKS,\n" +
			"         tl: NODES\n" +
			"      in\n" +
			"        cons (g (gateways), tl) -> return find_incf_gatewaysv2 (gateways,\n" +
			"                                                                id, tl)\n" +
			"      | cons (i (initial), tl) -> return find_incf_nodes_all (tl, id)\n" +
			"      | cons (f (finals), tl) -> return find_incf_finals (finals, id, tl)\n" +
			"      | cons (t (tasks), tl) -> return find_incf_tasks (tasks, id, tl)\n" +
			"      | nil -> return nil\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Find incf of gateways -----*)\n" +
			"\n" +
			"function\n" +
			"   find_incf_gatewaysv2 (gateways: GATEWAYS, id: ID, nextnodes: NODES): IDS\n" +
			"is\n" +
			"   case gateways\n" +
			"      var\n" +
			"         ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS,\n" +
			"         tl: GATEWAYS\n" +
			"      in\n" +
			"        cons (gateway (ident, pattern, sort, incf, outf), tl) ->\n" +
			"           if (ident == id) then\n" +
			"              return incf\n" +
			"           else\n" +
			"              return find_incf_gatewaysv2 (tl, id, nextnodes)\n" +
			"           end if\n" +
			"      | nil -> return find_incf_nodes_all (nextnodes, id)\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*----- Find incf of finals -----*)\n" +
			"\n" +
			"function find_incf_finals (finals: FINALS, id: ID, nextnodes: NODES): IDS is\n" +
			"   case finals\n" +
			"      var ident: ID, incf: IDS, tl: FINALS in\n" +
			"        cons (final (ident, incf), tl) ->\n" +
			"           if (ident == id) then\n" +
			"              return incf\n" +
			"           else\n" +
			"              return find_incf_finals (tl, id, nextnodes)\n" +
			"           end if\n" +
			"      | nil -> return find_incf_nodes_all (nextnodes, id)\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"(*-------- Find incf of taks ------------*)\n" +
			"\n" +
			"function find_incf_tasks (tasks: TASKS, id: ID, nextnodes: NODES): IDS is\n" +
			"   case tasks\n" +
			"      var ident: ID, incf: IDS, outf: IDS, tl: TASKS in\n" +
			"        cons (task (ident, incf, outf), tl) ->\n" +
			"           if (ident == id) then\n" +
			"              return incf\n" +
			"           else\n" +
			"              return find_incf_tasks (tl, id, nextnodes)\n" +
			"           end if\n" +
			"      | nil -> return find_incf_nodes_all (nextnodes, id)\n" +
			"   end case\n" +
			"end function\n" +
			"\n" +
			"------------------------------------------------------------------------------\n" +
			"\n" +
			"end module"
	;

	public BpmnTypesBuilder()
	{

	}

	public void dumpBpmnTypesFile()
	{
		final File bpmnTypesFile = new File(this.outputDirectory + File.separator + BPMN_TYPES_FILENAME);
		final PrintWriter printWriter;

		try
		{
			printWriter = new PrintWriter(bpmnTypesFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		printWriter.print(BPMN_TYPES);
		printWriter.flush();
		printWriter.close();
	}
}
