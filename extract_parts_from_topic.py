# edited by Alexandros 15/01/2018

import os
import sys
import re

def main(filename, format):
    record = False
    record2 = False
    topic_id = ""
    doc_dict = {}
    doc_dict_2 = {}
    
    ################ added by alex #################### 
    query = []
    #===============================================#
    
    with open(filename, "r") as f:
        while f:
            line = f.readline()
            if not line:
                break
    

            if record:
                doc_id = line.strip()
                passed = False

                if doc_id != "":
                    passed = True
                if doc_id.startswith("rev"):
                    passed = False
                if passed:
                    doc_dict[doc_id] = 1
            
            ################ added by alex #################### 
            if record2:
                doc_id_2 = line.strip()
                passed = False
                
                if doc_id_2 not in 'Pids:':
                    if not doc_id_2.isdigit():
                        if not 'review_ID:' in doc_id_2: 
                            if doc_id_2:
                                query.append(doc_id_2)
                                #print(query)
            #===============================================#   
        
            if line.startswith("Topic:"):
                topic_id = line.split()[1].strip()

            if line.startswith("Title:"):
                title = line.strip()[7:]
            
            ################ added by alex ####################
            if line.startswith("Query:"):
                record2 = True
           
            
            if line.startswith("Pids:"):
                record = True

    title_filename = "{0}.title".format(topic_id)
    with open(title_filename,"w") as wf:
        wf.write("{0} {1}\n".format(topic_id, title))
        wf.close()

    topic_filename =  "{0}.topicid".format(topic_id)
    with open(topic_filename,"w") as wf:
        wf.write("{0}\n".format(topic_id))
        wf.close()

    ###############  added by alex ####################    
    query_filename =  "{0}.topic.query".format(topic_id)
    new_query = " ".join(query)
    
    # Query Processing Variations
    # query
    #clean_query = new_query.replace('[MeSH]', ' ').replace(' cf ', ' ').replace(' bl ', ' ').replace(' di ', ' ').replace('.tw.', ' ').replace('.ti.', ' ').replace('[mesh]', ' ').replace('[Mesh]', ' ').replace('.ab.', ' ').replace('"', ' ').replace('[tiab]', ' ').replace(' tiab ', ' ').replace('OR', ' ').replace('[ab]', ' ').replace(' ab ', ' ').replace('#', ' ').replace('AND', ' ').replace('[tw]', ' ').replace(' tw ', ' ').replace('(', ' ').replace(')', ' ').replace('$', ' ').replace('or', ' ').replace('and', ' ').replace('=', ' ').replace('[ti]', ' ').replace(' ti ', ' ').replace('=', ' ').replace('+', ' ').replace('-', ' ').replace('.mp', ' ').replace('/', ' ').replace('not', ' ').replace('/', ' ').replace('NOT', ' ').replace('.sh.', ' ').replace('Current', ' ').replace('0', ' ').replace('1', ' ').replace('2', ' ').replace('3', ' ').replace('4', ' ').replace('5', ' ').replace('6', ' ').replace('7', ' ').replace('8', ' ').replace('9', ' ').replace('.au.', ' ').replace(' yr ', ' ').replace('exp ', ' ').replace('.', ' ').replace(',', ' ').replace('?', '').replace('[', ' ').replace(']', ' ').replace(' tw ', ' ').replace(' * ', ' ').replace(':', ' ').replace('{', ' ').replace('}', ' ').replace('@', ' ').replace(';', ' ').replace('!', ' ')
    
    #query2
    #clean_query = new_query.replace('[MeSH]', ' ').replace(' cf ', ' ').replace(' bl ', ' ').replace(' di ', ' ').replace('.tw.', ' ').replace('.ti.', ' ').replace('[mesh]', ' ').replace('[Mesh]', ' ').replace('.ab.', ' ').replace('"', ' ').replace('[tiab]', ' ').replace(' tiab ', ' ').replace('OR', ' ').replace('[ab]', ' ').replace(' ab ', ' ').replace('#', ' ').replace('AND', ' ').replace('[tw]', ' ').replace(' tw ', ' ').replace('(', ' ').replace(')', ' ').replace('$', ' ').replace('or', ' ').replace('and', ' ').replace('=', ' ').replace('[ti]', ' ').replace(' ti ', ' ').replace('=', ' ').replace('+', ' ').replace('-', ' ').replace('.mp', ' ').replace('/', ' ').replace('not', ' ').replace('/', ' ').replace('NOT', ' ').replace('.sh.', ' ').replace('Current', ' ').replace('0', ' ').replace('1', ' ').replace('2', ' ').replace('3', ' ').replace('4', ' ').replace('5', ' ').replace('6', ' ').replace('7', ' ').replace('8', ' ').replace('9', ' ').replace('.au.', ' ').replace(' yr ', ' ').replace('exp ', ' ').replace('.', ' ').replace(',', ' ').replace('?', '').replace('[', ' ').replace(']', ' ').replace(' tw ', ' ').replace(' * ', ' ').replace(':', ' ').replace('{', ' ').replace('}', ' ').replace('@', ' ').replace(';', ' ').replace('!', ' ').replace('ti ab', ' ').replace('Exp ', ' ').replace('adj ', ' ').replace('$', '*')
    
    #query3 - first attempt
    #clean_query = new_query.replace('[MeSH]', ' ').replace('.rn', ' ').replace(' cf ', ' ').replace('.ti,ab.', ' ').replace(' bl ', ' ').replace(' di ', ' ').replace('.tw.', ' ').replace('.ti.', ' ').replace('[mesh]', ' ').replace('[Mesh]', ' ').replace('.ab.', ' ').replace('"', ' ').replace('[tiab]', ' ').replace(' tiab ', ' ').replace('OR', ' ').replace('[ab]', ' ').replace(' ab ', ' ').replace('#', ' ').replace('AND', ' ').replace('[tw]', ' ').replace(' tw ', ' ').replace('(', ' ').replace(')', ' ').replace('$', ' ').replace('or', ' ').replace('Or', ' ').replace('and', ' ').replace('=', ' ').replace('[ti]', ' ').replace(' ti ', ' ').replace('=', ' ').replace('+', ' ').replace('-', ' ').replace('.mp', ' ').replace('/', ' ').replace('not', ' ').replace('/', ' ').replace('NOT', ' ').replace('.sh.', ' ').replace('Current', ' ').replace('0', ' ').replace('1', ' ').replace('2', ' ').replace('3', ' ').replace('4', ' ').replace('5', ' ').replace('6', ' ').replace('7', ' ').replace('8', ' ').replace('9', ' ').replace('.au.', ' ').replace(' yr ', ' ').replace('exp ', ' ').replace('.', ' ').replace(',', ' ').replace('?', '').replace('[', ' ').replace(']', ' ').replace(' tw ', ' ').replace(' * ', ' ').replace(':', ' ').replace('{', ' ').replace('}', ' ').replace('@', ' ').replace(';', ' ').replace('!', ' ').replace('ti ab', ' ').replace('Exp ', ' ').replace('adj ', ' ').replace('$', '*').replace('.adj.', ' ').replace('.adj', ' ').replace('adj.', ' ').replace('adj3', ' ').replace('adj2', ' ').replace('adj', ' ').replace(' rK ', ' ').replace(' K ', ' ')
    
    #query4 - or query3 second attempt
    #clean_query = new_query.replace('[MeSH]', ' ').replace('[Mesh:NoExp]', ' ').replace('[Majr]', ' ').replace('[mh]', ' ').replace('[mh:noexp]', ' ').replace('[nm]', ' ').replace('[pa]', ' ').replace('[sh]', ' ').replace('[sh:noexp]', ' ').replace('[All]', ' ').replace('[ot]', ' ').replace('.ot.', ' ').replace('.ot', ' ').replace('ot ', ' ').replace('[ps]', ' ').replace('.ps.', ' ').replace('.ps', ' ').replace('.in.', ' ').replace('.in', ' ').replace('[pg]', ' ').replace('.pg', ' ').replace('[journal]', ' ').replace('[ta]', ' ').replace('.ta.', ' ').replace('.ta', ' ').replace('[la]', ' ').replace('.la', ' ').replace('[lid]', ' ').replace('.lid.', ' ').replace('.lid', ' ').replace('[aid]', ' ').replace('[auid]', ' ').replace('.adj.', ' ').replace('.adj', ' ').replace('adj.', ' ').replace('adj3', ' ').replace('adj2', ' ').replace('adj ', ' ').replace(' cf ', ' ').replace('.ti,ab.', ' ').replace(' bl ', ' ').replace(' di ', ' ').replace('.tw.', ' ').replace('.ti.', ' ').replace('[mesh]', ' ').replace('[Mesh]', ' ').replace('.ab.', ' ').replace('"', ' ').replace('Not', ' ').replace('[tiab]', ' ').replace(' tiab ', ' ').replace('OR', ' ').replace('[ab]', ' ').replace(' ab ', ' ').replace('#', ' ').replace('And', ' ').replace('AND', ' ').replace('[tw]', ' ').replace(' tw ', ' ').replace('(', ' ').replace(')', ' ').replace('$', ' ').replace('or', ' ').replace('Or', ' ').replace('and', ' ').replace('=', ' ').replace('[ti]', ' ').replace(' ti ', ' ').replace('=', ' ').replace('+', ' ').replace('-', ' ').replace('.mp', ' ').replace('/', ' ').replace('not', ' ').replace('/', ' ').replace('NOT', ' ').replace('.sh.', ' ').replace('Current', ' ').replace('0', ' ').replace('1', ' ').replace('2', ' ').replace('3', ' ').replace('4', ' ').replace('5', ' ').replace('6', ' ').replace('7', ' ').replace('8', ' ').replace('9', ' ').replace('.au.', ' ').replace(' yr ', ' ').replace('exp ', ' ').replace('.', ' ').replace(',', ' ').replace('?', '').replace('[', ' ').replace(']', ' ').replace(' tw ', ' ').replace(' * ', ' ').replace(':', ' ').replace('{', ' ').replace('}', ' ').replace('@', ' ').replace(';', ' ').replace('!', ' ').replace('ti ab', ' ').replace('Exp ', ' ').replace('$', '*')
    
    #query5
    #clean_query = new_query.replace('[MeSH]', ' ').replace('.tw,ot.', ' ').replace('[Mesh:NoExp]', ' ').replace('/di', ' ').replace('/du', ' ').replace('pa, ra', ' ').replace('[Majr]', ' ').replace('.fs.', ' ').replace('.fs', ' ').replace('fs ', ' ').replace('[dp]', ' ').replace('[mh]', ' ').replace('[mh:noexp]', ' ').replace('[nm]', ' ').replace('[pa]', ' ').replace('[sh]', ' ').replace('[sh:noexp]', ' ').replace('[All]', ' ').replace('[ot]', ' ').replace('.ot.', ' ').replace('.ot', ' ').replace(' ot ', ' ').replace('[ps]', ' ').replace('.ps.', ' ').replace('.ps', ' ').replace('.pt.', ' ').replace('.in.', ' ').replace('.in', ' ').replace('[pg]', ' ').replace('.pg', ' ').replace('[ta]', ' ').replace('.ta.', ' ').replace('.ta', ' ').replace('[la]', ' ').replace('.la', ' ').replace('[lid]', ' ').replace('.lid.', ' ').replace('.lid', ' ').replace('[aid]', ' ').replace('[auid]', ' ').replace('.adj.', ' ').replace('.adj', ' ').replace('adj.', ' ').replace('adj3', ' ').replace('adj2', ' ').replace('adj ', ' ').replace(' cf ', ' ').replace('.ti,ab.', ' ').replace(' bl ', ' ').replace(' di ', ' ').replace('.tw.', ' ').replace('.ti.', ' ').replace('[mesh]', ' ').replace('[Mesh]', ' ').replace('.ab.', ' ').replace('"', ' ').replace('Not', ' ').replace('[tiab]', ' ').replace(' tiab ', ' ').replace('OR', ' ').replace('[ab]', ' ').replace(' ab ', ' ').replace('#', ' ').replace('And', ' ').replace('AND', ' ').replace('[tw]', ' ').replace(' tw ', ' ').replace('(', ' ').replace(')', ' ').replace('$', ' ').replace('or', ' ').replace('Or', ' ').replace('and', ' ').replace('=', ' ').replace('[ti]', ' ').replace(' ti ', ' ').replace('=', ' ').replace('+', ' ').replace('-', ' ').replace('.mp.', ' ').replace('.mp', ' ').replace('mp', ' ').replace('\\\\', ' ').replace('not', ' ').replace('/', ' ').replace('NOT', ' ').replace('.sh.', ' ').replace('Current', ' ').replace('0', ' ').replace('1', ' ').replace('2', ' ').replace('3', ' ').replace('4', ' ').replace('5', ' ').replace('6', ' ').replace('7', ' ').replace('8', ' ').replace('9', ' ').replace('.au.', ' ').replace(' yr ', ' ').replace('exp ', ' ').replace('.', ' ').replace(',', ' ').replace('?', '').replace('[', ' ').replace(']', ' ').replace(' tw ', ' ').replace(' * ', ' ').replace(':', ' ').replace('{', ' ').replace('}', ' ').replace('@', ' ').replace(';', ' ').replace('!', ' ').replace('ti ab', ' ').replace('Exp ', ' ').replace('$', '*')
    
    #query6            [gr]  .cm.           .pp.    .pb.  [pubn]  .pi.      .vi.   
    clean_query = new_query.replace('[MeSH]', ' ').replace('.ax.', ' ').replace('limit[previous search]', ' ').replace('limit [previous search]', ' ').replace('.vo.', ' ').replace('[vi]', ' ').replace('[lang]', ' ').replace('[lr]', ' ').replace('[ptyp]', ' ').replace('.st.', ' ').replace('.cs.', ' ').replace('.ig.', ' ').replace('.nt.', ' ').replace('[aid]', ' ').replace('.fa.', ' ').replace('[pt]', ' ').replace('.ci.', ' ').replace('.cr.', ' ').replace('.tw,ot.', ' ').replace('[dcom]', ' ').replace('[crdt]', ' ').replace('[edat]', ' ').replace('[Mesh:NoExp]', ' ').replace('.tc.', ' ').replace('/di', ' ').replace('/du', ' ').replace('pa, ra', ' ').replace('[Majr]', ' ').replace('.fs.', ' ').replace('.fs', ' ').replace('fs ', ' ').replace('[dp]', ' ').replace('[dep]', ' ').replace('[mh]', ' ').replace('[mh:noexp]', ' ').replace('[nm]', ' ').replace('[pa]', ' ').replace('.pa.', ' ').replace('.pa', ' ').replace('[sh]', ' ').replace('[sh:noexp]', ' ').replace('[All]', ' ').replace('[ot]', ' ').replace('.ot.', ' ').replace('.ot', ' ').replace(' ot ', ' ').replace('[ps]', ' ').replace('.ps.', ' ').replace('.ps', ' ').replace('.pt.', ' ').replace('.in.', ' ').replace('.in', ' ').replace('[pg]', ' ').replace('.pg', ' ').replace('[ta]', ' ').replace('.ta.', ' ').replace('.ta', ' ').replace('[la]', ' ').replace('.la', ' ').replace('[lid]', ' ').replace('.lid.', ' ').replace('.lid', ' ').replace('[aid]', ' ').replace('[auid]', ' ').replace('.adj.', ' ').replace('.adj', ' ').replace('adj.', ' ').replace('adj3', ' ').replace('adj2', ' ').replace('adj ', ' ').replace(' cf ', ' ').replace('.ti,ab.', ' ').replace(' bl ', ' ').replace(' di ', ' ').replace('.tw.', ' ').replace('.ti.', ' ').replace('[mesh]', ' ').replace('[Mesh]', ' ').replace('.ab.', ' ').replace('"', ' ').replace('Not', ' ').replace('[tiab]', ' ').replace(' tiab ', ' ').replace('OR', ' ').replace('[ab]', ' ').replace(' ab ', ' ').replace('#', ' ').replace('And', ' ').replace('AND', ' ').replace('[tw]', ' ').replace(' tw ', ' ').replace('(', ' ').replace(')', ' ').replace('$', ' ').replace('or', ' ').replace('Or', ' ').replace('and', ' ').replace('=', ' ').replace('[ti]', ' ').replace(' ti ', ' ').replace('=', ' ').replace('+', ' ').replace('-', ' ').replace('.mp.', ' ').replace('.mp', ' ').replace('mp', ' ').replace('\\\\', ' ').replace('not', ' ').replace('/', ' ').replace('NOT', ' ').replace('.sh.', ' ').replace('Current', ' ').replace('0', ' ').replace('1', ' ').replace('2', ' ').replace('3', ' ').replace('4', ' ').replace('5', ' ').replace('6', ' ').replace('7', ' ').replace('8', ' ').replace('9', ' ').replace('.au.', ' ').replace('.au', ' ').replace('au.', ' ').replace(' yr ', ' ').replace('exp ', ' ').replace('.', ' ').replace(',', ' ').replace('?', '').replace('[', ' ').replace(']', ' ').replace(' tw ', ' ').replace(' * ', ' ').replace(':', ' ').replace('{', ' ').replace('}', ' ').replace('@', ' ').replace(';', ' ').replace('!', ' ').replace('ti ab', ' ').replace('Exp ', ' ').replace('$', '*')
    
    clean_query_removed_non_ascii = re.sub(r'[^\x00-\x7F]+',' ', clean_query)
    new_clean_query = re.sub(' +',' ',clean_query_removed_non_ascii)
    with open(query_filename,"w") as wf:
        wf.write("{0} {1}".format(topic_id,new_clean_query))
        wf.close()
    #===============================================#    
        
    pid_filename =  "{0}.pids".format(topic_id)
    with open(pid_filename, "w") as wf:
        rank = 0
        for doc_id in doc_dict.keys():
            if format == 'TREC':
                rank = rank + 1
                score = -float(rank)+0.0
                wf.write("{0} NF {1} {2} {3} pubmed\n".format(topic_id, doc_id, rank, score ))
            else:
                wf.write("{0} {1}\n".format(topic_id, doc_id))
        wf.close()

def usage(args):
    print("Usage: {0} <filename> <format> where <format> is TREC or TOP".format(args[0]))
    print("Given a TAR file i.e. train_topic/44 or train_topic/38")
    print("this script extracts out the different parts, title, pids and topicid ")
    print("It does not currently extract the query")
    print("The output is three files prefixed with the topicid")
    print("\t<topicid>.pids\t contains the list of pids retrieved by the pub med query")
    print("\t<topicid>.title\t contains the title of the topic")
    print("\t<topicid>.topicid\t contains the topicid")
    
    ###############  added by alex #################### 
    # the script now extracts the query
    print("\t<topicid>.topic.query\t contains the query of the topic")
    #===============================================#


if __name__ == "__main__":
    filename = None
    format = "TOP"

    if len(sys.argv) >= 2:
        filename = sys.argv[1]
    else:
        usage(sys.argv)
        exit(1)

    if len(sys.argv)==3:
        format = sys.argv[2].upper()

    if os.path.exists( filename ) and format in ['TREC', 'TOP']:
        exit(main(filename,format))
    else:
        usage(sys.argv)
        exit(1)

