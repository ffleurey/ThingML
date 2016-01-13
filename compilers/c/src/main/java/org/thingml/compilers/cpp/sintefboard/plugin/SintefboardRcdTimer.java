/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.compilers.cpp.sintefboard.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sintef.thingml.Configuration;
import org.sintef.thingml.ExternalConnector;
import org.sintef.thingml.Message;
import org.sintef.thingml.Port;
import org.sintef.thingml.Thing;
import org.thingml.compilers.c.CCompilerContext;
import org.thingml.compilers.c.CNetworkLibraryGenerator;

/**
 *
 * @author sintef
 */
public class SintefboardRcdTimer extends CNetworkLibraryGenerator {

    public SintefboardRcdTimer(Configuration cfg, CCompilerContext ctx) {
        super(cfg, ctx);
    }
    public SintefboardRcdTimer(Configuration cfg, CCompilerContext ctx, Set<ExternalConnector> ExternalConnectors) {
        super(cfg, ctx, ExternalConnectors);
    }

    @Override
    public boolean isGeneratingCpp() {
        return true;
    }

    @Override
    public String getCppNameScope() {
        return "/*CFG_CPPNAME_SCOPE*/";
    }
    
    @Override
    public void generateNetworkLibrary() {
        CCompilerContext ctx = (CCompilerContext) this.ctx;


        if(!this.getExternalConnectors().isEmpty()) {
            String ctemplate = ctx.getNetworkLibRcdTimerCommonTemplate();
            String htemplate = ctx.getNetworkLibRcdTimerCommonHeaderTemplate();
            int timerInstances = findTimerInstances();

            ctemplate = ctemplate.replace("/*RCD_TIMER_INSTANCES*/", ""+timerInstances);
            htemplate = htemplate.replace("/*RCD_TIMER_INSTANCES*/", ""+timerInstances);

            ctx.getBuilder("Rcdtimer_common.c").append(ctemplate);
            ctx.getBuilder("Rcdtimer_common.h").append(htemplate);
        }
        
        for(ExternalConnector eco : this.getExternalConnectors()) {
            //boolean ring = false;
            String ctemplate = ctx.getNetworkLibRcdTimerInstanceTemplate();
            String htemplate = ctx.getNetworkLibRcdTimerInstanceHeaderTemplate();

            String portName;
            if(eco.hasAnnotation("port_name")) {
                portName = eco.annotation("port_name").iterator().next();
            } else {
                portName = eco.getProtocol();
            }

            eco.setName(portName);

            ctemplate = ctemplate.replace("/*PORT_NAME*/", portName);
            htemplate = htemplate.replace("/*PORT_NAME*/", portName);


            //Connector Instanciation
            StringBuilder eco_instance = new StringBuilder();
            eco_instance.append("//Connector");
            Port p = eco.getPort();
            if(!p.getReceives().isEmpty()) {
            //if(!p.getSends().isEmpty()) {
                eco_instance.append("// Pointer to receiver list\n");
                eco_instance.append("struct Msg_Handler ** ");
                eco_instance.append(p.getName());
                eco_instance.append("_receiver_list_head;\n");

                eco_instance.append("struct Msg_Handler ** ");
                eco_instance.append(p.getName());
                eco_instance.append("_receiver_list_tail;\n");
            }

            if(!p.getSends().isEmpty()) {
            //if(!p.getReceives().isEmpty()) {
                eco_instance.append("// Handler Array\n");
                eco_instance.append("struct Msg_Handler * ");
                eco_instance.append(p.getName());
                eco_instance.append("_handlers;\n");
            }
            ctemplate = ctemplate.replace("/*INSTANCE_INFORMATION*/", eco_instance);

            ctx.getBuilder(eco.getInst().getInstance().getName() + "_" + eco.getPort().getName() + "_" + eco.getProtocol() + ".c").append(ctemplate);
            ctx.getBuilder(eco.getInst().getInstance().getName() + "_" + eco.getPort().getName() + "_" + eco.getProtocol() + ".h").append(htemplate);

        }
    }

    private int findTimerInstances() {
        int ret = 0;
        CCompilerContext ctx = (CCompilerContext) this.ctx;
        
        for(ExternalConnector eco : this.getExternalConnectors()) {
            String timername = eco.getProtocol();
            System.out.println("findTimerInstance() found <" + timername + ">");
            int timernum = Integer.decode(timername.replace("Rcdtimer", ""));
            if (ret < timernum)
                ret = timernum;
        }
        return ret+1;
    }
            
    @Override
    public void generateMessageForwarders(StringBuilder builder, StringBuilder headerbuilder) {
        CCompilerContext ctx = (CCompilerContext) this.ctx;
        if(!this.getExternalConnectors().isEmpty()) {

            //************ Generate methods for sending meassages to timers
            for (ExternalConnector eco : this.getExternalConnectors()) {
                Thing t = eco.getInst().getInstance().getType();
                Port p = eco.getPort();

                for (Message m : p.getSends()) {
                    Set<String> ignoreList = new HashSet<String>();
                    List<String> paramList;

                    headerbuilder.append("// Forwarding of messages " + eco.getName() + "::" + t.getName() + "::" + p.getName() + "::" + m.getName() + "\n");
                    headerbuilder.append("void " + "forward_" + eco.getName() + "_" + ctx.getSenderName(t, p, m));
                    ctx.appendFormalParameters(t, headerbuilder, m);
                    headerbuilder.append(";\n");

                    builder.append("// Forwarding of messages " + eco.getName() + "::" + t.getName() + "::" + p.getName() + "::" + m.getName() + "\n");
                    builder.append("void " + getCppNameScope() + "forward_" + eco.getName() + "_" + ctx.getSenderName(t, p, m));
                    ctx.appendFormalParameters(t, builder, m);
                    builder.append("{\n");

                    String timername = eco.getName();
                    String timernum = timername.replace("Rcdtimer", "");

                    if (m.getName().contentEquals("timer_start")) {
                        builder.append("rcd_timer_start(" + timernum + ctx.getActualParametersSection(m) + ");\n");
                    } else if (m.getName().contentEquals("timer_cancel")) {
                        builder.append("rcd_timer_cancel(" + timernum + ");\n");
                    } 

                    builder.append("}\n");
                }
            }

            //************ Generate methods for receiving messages from timers

            headerbuilder.append("// Receive forwarding of messages from timers\n");
            headerbuilder.append("void " + "rcd_send_timeout(unsigned int timer_num)");
            headerbuilder.append(";\n");
            builder.append("// Receive forwarding of messages from timers\n");
            builder.append("void " + getCppNameScope() + "rcd_send_timeout(unsigned int timer_num)");
            builder.append("{\n");
            builder.append("switch (timer_num) {\n");
            for (ExternalConnector eco : this.getExternalConnectors()) {
                Thing t = eco.getInst().getInstance().getType();
                Port p = eco.getPort();
                String timername = eco.getName();
                String timernum = timername.replace("Rcdtimer", "");
                builder.append("//timernum is() " + timernum + "\n");
                builder.append("case " + timernum + ":\n");
                generateTimerReceiver(timername, p, builder);
                builder.append("break;\n");
            }
            builder.append("} // switch from timer\n");
            builder.append("}\n");
        }
        
        
    }

    private void generateTimerReceiver(String timername, Port p, StringBuilder builder) {
        CCompilerContext ctx = (CCompilerContext) this.ctx;
        
        for (Message m : p.getReceives()) {
            Set<String> ignoreList = new HashSet<String>();
            builder.append("{\n");
            ctx.appendFormalParameterDeclarations(builder, m);
            builder.append("{\n");
            ctx.generateSerializationForForwarder(m, builder, ctx.getHandlerCode(cfg, m), ignoreList);
            builder.append("externalMessageEnqueue(forward_buf, " + (ctx.getMessageSerializationSize(m) - 2) + ", " + timername + "_instance.listener_id);\n");
            builder.append("}\n");
            builder.append("}\n");
        }
    }

    @Override
    public void generatePollCode(StringBuilder builder) {
        if(!this.getExternalConnectors().isEmpty()) {
            builder.append("rcd_timer_check(); // Expired timer will result in more to process\n");
        }
    }
    

}
