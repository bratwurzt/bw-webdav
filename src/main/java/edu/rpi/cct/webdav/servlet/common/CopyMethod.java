/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:
        
    http://www.apache.org/licenses/LICENSE-2.0
        
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package edu.rpi.cct.webdav.servlet.common;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;
import edu.rpi.cct.webdav.servlet.shared.WebdavNotFound;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class to handle COPY
 *
 *   @author Mike Douglass   douglm@rpi.edu
 */
public class CopyMethod extends MethodBase {
  /** Called at each request
   */
  public void init() {
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.common.MethodBase#doMethod(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doMethod(HttpServletRequest req,
                       HttpServletResponse resp) throws WebdavException {
    process(req, resp, true);
  }

  protected void process(HttpServletRequest req,
                         HttpServletResponse resp,
                         boolean copy) throws WebdavException {
    if (debug) {
      if (copy) {
        trace("CopyMethod: doMethod");
      } else {
        trace("MoveMethod: doMethod");
      }
    }

    try {
      String dest = req.getHeader("Destination");
      if (dest == null) {
        if (debug) {
          debugMsg("No Destination");
        }
        throw new WebdavNotFound("No Destination");
      }

      int depth = Headers.depth(req);
      /*
      if (depth == Headers.depthNone) {
        depth = Headers.depthInfinity;
      }
      */

      String ow = req.getHeader("Overwrite");
      boolean overwrite;
      if (ow == null) {
        overwrite = true;
      } else if ("T".equals(ow)) {
        overwrite = true;
      } else if ("F".equals(ow)) {
        overwrite = false;
      } else {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      WebdavNsIntf intf = getNsIntf();
      WebdavNsNode from = intf.getNode(getResourceUri(req),
                                       WebdavNsIntf.existanceMust,
                                       WebdavNsIntf.nodeTypeUnknown);

      if ((from == null) || !from.getExists()) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      int toNodeType;
      if (from.isCollection()) {
        toNodeType = WebdavNsIntf.nodeTypeCollection;
      } else {
        toNodeType = WebdavNsIntf.nodeTypeEntity;
      }

      WebdavNsNode to = intf.getNode(intf.getUri(dest),
                                     WebdavNsIntf.existanceMay, toNodeType);

      if (from.equals(to)) {
        throw new WebdavForbidden("source and destination equal");
      }

      intf.copyMove(req, resp, from, to, copy, overwrite, depth);
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }
}