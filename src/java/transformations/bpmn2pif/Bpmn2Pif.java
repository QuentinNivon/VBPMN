/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * fmt
 * Copyright (C) 2014 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

package transformations.bpmn2pif;

// java
import java.io.File;
import java.io.IOException;
// fmt
import models.base.*;
import transformations.base.Transformer;
// vbpmn
import models.process.bpmn.BpmnModel;
import models.process.pif.PifModel;
import models.process.bpmn.BpmnEMFBpmnReader;
import models.process.pif.PifPifWriter;

public class Bpmn2Pif {
    public static final String USAGE = "Bpmn2Pif input_file output_file";

    public static void main(String[] args) {
        Transformer trans = new Bpmn2PifTransformer();
        trans.setVerbose(true);
        trans.about();
        if (args.length != 2) {
            trans.error(USAGE);
            return;
        }
        try {
            AbstractModelReader reader = new BpmnEMFBpmnReader();
            AbstractModelWriter writer = new PifPifWriter();
            AbstractModel input_model = new BpmnModel();
            input_model.setResource(new File(args[0]));
            AbstractModel output_model = new PifModel();
            output_model.setResource(new File(args[1]));
            trans.setResources(input_model, output_model, reader, writer);
            trans.run(false);
            trans.cleanUp();
        } catch (IOException | IllegalResourceException | IllegalModelException e) {
            trans.message(e.getMessage());
            e.printStackTrace();
        }
    }
}
