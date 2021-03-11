/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * vbpmn
 * Copyright (C) 2015 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

package transformations.pif2dot;

import models.base.AbstractModel;
import models.base.AbstractModelReader;
import models.base.IllegalModelException;
import models.base.IllegalResourceException;
import models.process.pif.DotPifWriter;
import models.process.pif.PifModel;
import models.process.pif.PifPifReader;
import transformations.base.AbstractTransformer;
import transformations.base.Transformer;

import java.io.File;
import java.io.IOException;

public class Pif2Dot {
    public static final String USAGE = "Pif2Dot pif_schema_path input_file output_file";
    public static final String NAME = "bpmn2pif";
    public static final String VERSION = "1.0";

    public static void main(String[] args) {
        // fake transformer, we use directly a Writer
        Transformer trans = new AbstractTransformer() {
            @Override
            public void transform() throws IllegalResourceException, IllegalModelException {}
            @Override
            public void about() { System.out.println(NAME + " " + VERSION); }
        };
        trans.setVerbose(true);
        trans.about();
        if (args.length != 3) {
            trans.error(USAGE);
            return;
        }
        try {
            // set the schema path
            String schema_path = args[0];
            // read the input model
            AbstractModelReader reader = new PifPifReader();
            ((PifPifReader)reader).setSchemaPath(schema_path);
            AbstractModel model = new PifModel();
            model.setResource(new File(args[1]));
            model.modelFromFile(reader);
            // write the model using the good writer
            model.setResource(new File(args[2]));
            model.modelToFile(new DotPifWriter());
        } catch (IllegalResourceException | IllegalModelException | IOException e) {
            trans.error(e.getMessage());
        }
    }
}
