package fr.inria.convecs.optimus.py_to_java.cadp_compliance._2024h;

import fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics.BpmnTypesBuilderGeneric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class BpmnTypesBuilder extends BpmnTypesBuilderGeneric
{
	private static final String BPMN_TYPES_FILENAME = "bpmntypes.lnt";

	private static final String BPMN_TYPES = "\n" +
			"(*  BPMN data types (FACS'16), necessary for encoding unbalanced workflows\n" +
			"Author: Gwen Salaun *)\n" +
			"\n" +
			"module bpmntypes(id) with get, <, == is\n" +
			"\n" +
			"(* set of identifiers *)\n" +
			"\n" +
			"type IDS is !card 48000\n" +
			"    set of ID \n" +
			"with ==, !=, inter, card, empty, member, insert, union, remove, diff\n" +
			"end type\n" +
			"\n" +
			"\n" +
			"(* flow *)\n" +
			"\n" +
			"type FLOW is !card 100\n" +
			"    flow ( ident: ID, source: ID, target: ID )\n" +
			"end type\n" +
			"\n" +
			"(* set of flows *)\n" +
			"\n" +
			"type FLOWS is !card 100\n" +
			"    set of FLOW\n" +
			"end type\n" +
			"\n" +
			"(* task *)\n" +
			"\n" +
			"type TASK is !card 100\n" +
			"    task ( ident: ID, incf: IDS, outf: IDS )\n" +
			"end type\n" +
			"\n" +
			"(* set of tasks *)\n" +
			"\n" +
			"type TASKS is !card 100\n" +
			"    set of TASK\n" +
			"end type\n" +
			"\n" +
			"(* initial event *)\n" +
			"\n" +
			"type INITIAL is !card 100\n" +
			"    initial ( ident: ID, outf: ID )   (* several outgoing flows (?) *)\n" +
			"end type\n" +
			"\n" +
			"(* final event *)\n" +
			"\n" +
			"type FINAL is !card 100\n" +
			"    final ( ident: ID, incf: IDS )     (* several incoming flows (?) *)\n" +
			"end type\n" +
			"\n" +
			"(* set of final events *)\n" +
			"\n" +
			"type FINALS is !card 100\n" +
			"    set of FINAL\n" +
			"end type\n" +
			"\n" +
			"(* type of gateway *)\n" +
			"\n" +
			"type GSORT is\n" +
			"    xor, and, or\n" +
			"end type\n" +
			"\n" +
			"(* gateway pattern *)\n" +
			"\n" +
			"type GPATTERN is\n" +
			"    split, merge\n" +
			"end type\n" +
			"\n" +
			"(* gateway *)\n" +
			"\n" +
			"type GATEWAY is !card 100\n" +
			"    gateway ( ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS )\n" +
			"end type\n" +
			"\n" +
			"(* set of gateways *)\n" +
			"\n" +
			"type GATEWAYS is !card 100\n" +
			"    set of GATEWAY\n" +
			"end type\n" +
			"\n" +
			"(* node *)\n" +
			"\n" +
			"type NODE is !card 100   (* could it be simpler ? *)\n" +
			"    i ( initial: INITIAL ),\n" +
			"    f ( finals: FINALS ),\n" +
			"    g ( gateways: GATEWAYS ),\n" +
			"    t ( tasks: TASKS )\n" +
			"end type\n" +
			"\n" +
			"(* set of nodes *)\n" +
			"\n" +
			"type NODES is !card 100\n" +
			"    set of NODE\n" +
			"end type\n" +
			"\n" +
			"(* bpmn process *)\n" +
			"\n" +
			"type BPROCESS is !card 100\n" +
			"    proc ( name: ID, nodes: NODES, flows: FLOWS )  (* not the most optimized encoding for traversals *)\n" +
			"end type\n" +
			"\n" +
			"function is_merge_possible(p: BPROCESS, activeflows:IDS, mergeid:ID): Bool is\n" +
			"    var incf:IDS, active_merge:Nat, status:Bool in\n" +
			"        incf := find_incf(p, mergeid);\n" +
			"        active_merge := find_active_tokens(activeflows, incf);\n" +
			"        if(active_merge == 0) then\n" +
			"           status := False\n" +
			"        else\n" +
			"            status := True\n" +
			"        end if;\n" +
			"        return status\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"function find_incf(p: BPROCESS, mergeid:ID): IDS is\n" +
			"\tcase p\n" +
			"\tvar name: ID, nodes: NODES, flows: FLOWS in\n" +
			"           proc (name, nodes, flows) -> return find_incf_nodes(nodes, mergeid)\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"function find_incf_nodes (nodes: NODES, mergeid: ID): IDS is\n" +
			"\tcase nodes\n" +
			"\tvar gateways: GATEWAYS, initial: INITIAL, finals: FINALS, tasks: TASKS, tl: NODES in\n" +
			"           cons(g(gateways), tl) -> return find_incf_gateways(gateways, mergeid)\n" +
			"\t|  cons(i(initial), tl)\n" +
			"\t|  cons(f(finals), tl)\n" +
			"\t|  cons(t(tasks), tl)    -> return find_incf_nodes(tl, mergeid)\n" +
			"    |  nil -> return nil\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"function find_incf_gateways (gateways: GATEWAYS, mergeid: ID): IDS is\n" +
			"\tcase gateways\n" +
			"\tvar ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS, tl: GATEWAYS in\n" +
			"           cons(gateway(ident, pattern, sort, incf, outf), tl) ->\n" +
			"\t   \t\t   if (ident==mergeid) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return find_incf_gateways(tl,mergeid)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return nil\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"function find_active_tokens(activeflows:IDS, incf:IDS): Nat is\n" +
			"    var tokens:IDS, count:Nat in\n" +
			"        tokens := inter(activeflows, incf);\n" +
			"        count := card(tokens);\n" +
			"        return count\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"(*-------------------------------------------------------------------------------*)\n" +
			"(*--------------------Check for merge with BPMN 1.x semantics--------------------*)\n" +
			"(*-------------------------------------------------------------------------------*)\n" +
			"\n" +
			"function is_merge_possible_v2(p: BPROCESS, activeflows:IDS, mergeid:ID): Bool is\n" +
			"     var incf:IDS, inactiveincf:IDS, active_merge:Nat, visited: IDS, result1: Bool in\n" +
			"        visited := nil;\n" +
			"        incf := find_incf(p, mergeid);          (* just iterate through gateways instead of all nodes*)\n" +
			"        active_merge := find_active_tokens(activeflows, incf);\n" +
			"        (*--check if all the incf have tokens--*)\n" +
			"        if(active_merge == card(incf)) then\n" +
			"            return True\n" +
			"        else\n" +
			"            (*--first remove incf with active tokens--*)\n" +
			"            inactiveincf := remove_ids_from_set(activeflows, incf);\n" +
			"\n" +
			"            (*--then check upstream for remaining flows--*)\n" +
			"            result1 := check_af_upstream(!?visited, p, activeflows, inactiveincf);\n" +
			"            return result1\n" +
			"        end if\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"function is_sync_done(p:BPROCESS, activeflows: IDS, syncstore: IDS, mergeid:ID): Bool is \n" +
			"    var incf:IDS, activesync: IDS in\n" +
			"        incf := find_incf(p, mergeid);          (* just iterate through gateways instead of all nodes*)\n" +
			"        activesync := inter(activeflows, incf); \n" +
			"        if (empty(activesync)) then\n" +
			"            return False\n" +
			"        elsif (inter(activesync, syncstore) == activesync) then\n" +
			"            return True\n" +
			"        else\n" +
			"            return False\n" +
			"        end if\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"(*-------------------------- Merge check for parallel gateways----------------------------- *)\n" +
			"function is_merge_possible_par(p:BPROCESS, syncstore: IDS, mergeid:ID): Bool is \n" +
			"    var incf:IDS, activesync: IDS in\n" +
			"        incf := find_incf(p, mergeid);          (* just iterate through gateways instead of all nodes*)\n" +
			"        if (inter(incf, syncstore) == incf) then\n" +
			"            return True\n" +
			"        else\n" +
			"            return False\n" +
			"        end if\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"(*------------ finds all the upstream flows and checks for tokens -----------*)\n" +
			"\n" +
			"function check_af_upstream(in out visited:IDS, p:BPROCESS, activeflows:IDS, incf:IDS): Bool is\n" +
			"    var count:Nat, result1:Bool, result2:Bool in\n" +
			"        case incf\n" +
			"            var hd:ID, tl:IDS, upflow:IDS, source:ID in\n" +
			"            cons(hd, tl) ->\n" +
			"                        source := find_flow_source(p, hd);\n" +
			"                        if(source == DummyId) then\n" +
			"                                return True\n" +
			"                        elsif (member(source, visited)) then\n" +
			"                            result1 := check_af_upstream(!?visited, p, activeflows, tl);\n" +
			"                            return result1\n" +
			"                        else\n" +
			"                            visited := insert(source, visited);\n" +
			"                            upflow := get_incf_by_id(p, source);\n" +
			"                            if (upflow == nil) then\n" +
			"                                return True\n" +
			"                            end if;\n" +
			"                            count := find_active_tokens(activeflows, upflow);\n" +
			"                            if(count == 0 of Nat) then\n" +
			"                                result1 := check_af_upstream(!?visited, p, activeflows, upflow);\n" +
			"                                result2 := check_af_upstream(!?visited, p, activeflows, tl);\n" +
			"                                return result1 and result2\n" +
			"                            else\n" +
			"                                return False\n" +
			"                            end if\n" +
			"                        end if\n" +
			"            | nil -> return True\n" +
			"        end case\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"function find_flow_source(bpmn: BPROCESS, flowid: ID): ID is\n" +
			"    case bpmn\n" +
			"\tvar name: ID, nodes: NODES, flows: FLOWS in\n" +
			"           proc (name, nodes, flows) -> return traverse_flows(flows, flowid)\n" +
			"    end case\n" +
			"end function\n" +
			"\n" +
			"function traverse_flows(flows: FLOWS, flowid:ID): ID is\n" +
			"    var dummySource:ID in \n" +
			"    dummySource := DummyId;\n" +
			"    case flows\n" +
			"        var ident: ID, source: ID, target: ID, tl:FLOWS in\n" +
			"          cons(flow(ident, source, target), tl) ->  \n" +
			"                                        if (ident==flowid) then\n" +
			"                                            return source\n" +
			"                                        else\n" +
			"                                            return traverse_flows(tl, flowid)\n" +
			"                                        end if\n" +
			"        | nil -> return dummySource\n" +
			"    end case\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"(*------ given a node id, gets its incoming flows*)\n" +
			"function get_incf_by_id(p:BPROCESS, nodeid:ID): IDS is\n" +
			"    case p\n" +
			"\tvar name: ID, nodes: NODES, flows: FLOWS in\n" +
			"           proc (name, nodes, flows) -> return traverse_nodes(nodes, nodeid)\n" +
			"    end case\n" +
			"end function\n" +
			"\n" +
			"\n" +
			"(*------ Traverse across all nodes in search of the node ------*)\n" +
			"function traverse_nodes(nodes: NODES, id:ID): IDS is\n" +
			"    case nodes\n" +
			"        var gateways: GATEWAYS, initial: INITIAL, finals: FINALS, tasks: TASKS, tl: NODES, incf:IDS in\n" +
			"        cons(g(gateways), tl) ->\n" +
			"                                incf := traverse_gateways(gateways, id);\n" +
			"                                    if (nil == incf) then\n" +
			"                                        return traverse_nodes(tl, id)\n" +
			"                                    else\n" +
			"                                        return incf\n" +
			"                                    end if\n" +
			"        |  cons(i(initial), tl) -> return traverse_nodes(tl, id)\n" +
			"        |  cons(f(finals), tl) ->\n" +
			"                                incf := traverse_finals(finals, id);\n" +
			"                                    if (nil == incf) then\n" +
			"                                        return traverse_nodes(tl, id)\n" +
			"                                    else\n" +
			"                                        return incf\n" +
			"                                    end if\n" +
			"        |  cons(t(tasks), tl) ->\n" +
			"                                incf := traverse_tasks(tasks, id);\n" +
			"                                    if (nil == incf) then\n" +
			"                                        return traverse_nodes(tl, id)\n" +
			"                                    else\n" +
			"                                        return incf\n" +
			"                                    end if\n" +
			"        |  nil -> return nil\n" +
			"    end case\n" +
			"end function\n" +
			"\n" +
			"\n" +
			"(*-------- Find incf of gateways ------------*)\n" +
			"function traverse_gateways(gateways: GATEWAYS, id: ID): IDS is\n" +
			"\tcase gateways\n" +
			"\tvar ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS, tl: GATEWAYS in\n" +
			"           cons(gateway(ident, pattern, sort, incf, outf), tl) ->\n" +
			"\t   \t\t   if (ident==id) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return traverse_gateways(tl, id)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return nil\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"(*-------- Find incf of finals ------------*)\n" +
			"function traverse_finals(finals: FINALS, id: ID): IDS is\n" +
			"\tcase finals\n" +
			"\tvar ident: ID, incf: IDS, tl: FINALS in\n" +
			"           cons(final(ident, incf), tl) ->\n" +
			"\t   \t\t   if (ident==id) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return traverse_finals(tl, id)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return nil\n" +
			"    end case\n" +
			"end function\n" +
			"\n" +
			"(*-------- Find incf of taks ------------*)\n" +
			"function traverse_tasks(tasks: TASKS, id: ID): IDS is\n" +
			"\tcase tasks\n" +
			"\tvar ident: ID, incf: IDS, outf: IDS, tl: TASKS in\n" +
			"           cons(task(ident, incf, outf), tl) ->\n" +
			"\t   \t\t   if (ident==id) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return traverse_tasks(tl, id)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return nil\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"\n" +
			"(*---------- Remove Incoming flows from activetokens ----------------------*)\n" +
			"function remove_incf(bpmn:BPROCESS, activeflows:IDS, mergeid:ID): IDS is\n" +
			"    var incf:IDS in\n" +
			"        incf := get_incf_by_id(bpmn, mergeid);\n" +
			"        return remove_ids_from_set(incf, activeflows)\n" +
			"    end var\n" +
			"end function\n" +
			"\n" +
			"function remove_sync(bpmn:BPROCESS, syncstore:IDS, mergeid:ID): IDS is\n" +
			"    return remove_incf(bpmn, syncstore, mergeid)\n" +
			"end function\n" +
			"\n" +
			"\n" +
			"(*--------- Helper functions to remove a set of IDS from the set ---------------- *)\n" +
			"function remove_ids_from_set(toremove:IDS, inputset: IDS): IDS is\n" +
			"return diff (inputset, toremove) \n" +
			"end function\n" +
			"\n" +
			"\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"(*-----------------Another version of code for process node traversal---------------------------*)\n" +
			"(*---------------- Fix: Remove the code from final version -------------------------------------*)\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"(*----------------------------------------------------------------------------------------------*)\n" +
			"\n" +
			"(*------ Traverse across all nodes in search of the node ------*)\n" +
			"function find_incf_nodes_all(nodes: NODES, id:ID): IDS is\n" +
			"    case nodes\n" +
			"        var gateways: GATEWAYS, initial: INITIAL, finals: FINALS, tasks: TASKS, tl: NODES in\n" +
			"        cons(g(gateways), tl) -> return find_incf_gatewaysv2(gateways, id, tl)\n" +
			"        |  cons(i(initial), tl) -> return find_incf_nodes_all(tl, id)\n" +
			"        |  cons(f(finals), tl) -> return find_incf_finals(finals, id, tl)\n" +
			"        |  cons(t(tasks), tl) -> return find_incf_tasks(tasks, id, tl)\n" +
			"        |  nil -> return nil\n" +
			"    end case\n" +
			"end function\n" +
			"\n" +
			"(*-------- Find incf of gateways ------------*)\n" +
			"function find_incf_gatewaysv2(gateways: GATEWAYS, id: ID, nextnodes: NODES): IDS is\n" +
			"\tcase gateways\n" +
			"\tvar ident: ID, pattern: GPATTERN, sort: GSORT, incf: IDS, outf: IDS, tl: GATEWAYS in\n" +
			"           cons(gateway(ident, pattern, sort, incf, outf), tl) ->\n" +
			"\t   \t\t   if (ident==id) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return find_incf_gatewaysv2(tl, id, nextnodes)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return find_incf_nodes_all(nextnodes, id)\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"(*-------- Find incf of finals ------------*)\n" +
			"function find_incf_finals(finals: FINALS, id: ID, nextnodes: NODES): IDS is\n" +
			"\tcase finals\n" +
			"\tvar ident: ID, incf: IDS, tl: FINALS in\n" +
			"           cons(final(ident, incf), tl) ->\n" +
			"\t   \t\t   if (ident==id) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return find_incf_finals(tl, id, nextnodes)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return find_incf_nodes_all(nextnodes, id)\n" +
			"    end case\n" +
			"end function\n" +
			"\n" +
			"(*-------- Find incf of taks ------------*)\n" +
			"function find_incf_tasks(tasks: TASKS, id: ID, nextnodes: NODES): IDS is\n" +
			"\tcase tasks\n" +
			"\tvar ident: ID, incf: IDS, outf: IDS, tl: TASKS in\n" +
			"           cons(task(ident, incf, outf), tl) ->\n" +
			"\t   \t\t   if (ident==id) then\n" +
			"\t\t\t      return incf\n" +
			"\t\t\t   else\n" +
			"\t\t\t      return find_incf_tasks(tl, id, nextnodes)\n" +
			"\t\t\t   end if\n" +
			"        |  nil -> return find_incf_nodes_all(nextnodes, id)\n" +
			"        end case\n" +
			"end function\n" +
			"\n" +
			"end module\n";

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
