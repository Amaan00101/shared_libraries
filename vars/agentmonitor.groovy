// package monitor
import hudson.model.*;
import hudson.util.*;
import jenkins.model.*;
import hudson.FilePath.FileCallable;
import hudson.slaves.OfflineCause;
import hudson.node_monitors.*;

class AgentMonitor {

  public Map findAllNodesWithLowDiskFreeSpace(minSpaceInGB)
  {
    Map mapNodeToFreeSize = [:]
    Map lowFreeSpaceNodes = [:]
    def nodesToCheck = Jenkins.instance.nodes  

    for (node in nodesToCheck) {
      println("=== Begin to handle node ${node.name} ===")
      try {
        def computer = node.toComputer()

        if (computer.getChannel() == null) continue

        //Jenkins return null if size is zero.
        def diskSpace = DiskSpaceMonitor.DESCRIPTOR.get(computer)
        def size=0
        if (diskSpace != null) {
          size = diskSpace.size
        }
        def roundedSize = (size / (1024f * 1024f * 1024f)).round(2)

        println("node: " + node.getDisplayName() + ", free space: " + roundedSize + "GB")
        mapNodeToFreeSize[node.name.toString()] = roundedSize
        if (roundedSize < minSpaceInGB) {
          lowFreeSpaceNodes[node.name.toString()] = roundedSize
        }
      } catch(e) {
        println("ERROR: exception: " + e.toString())
        e.printStackTrace()
      }
    }

    return lowFreeSpaceNodes
  }

   public String generateDescriptionForLowDiskFreeSpaec(minSpaceInGB, lowFreeSpaceNodes)
   {
        def desc = StringBuilder.newInstance()
        if(lowFreeSpaceNodes.size() > 0) {
            desc << "<html>"
            desc << "<body>"
            desc << "<h1>Found some nodes, which have free space lower than ${minSpaceInGB}G</h1>"
            desc << "Low Free Space Nodes:"
            desc << "<table border=1>"
            lowFreeSpaceNodes.each{node, freeSpace -> 
              desc << "<tr>"
              desc <<   "<th align='left'>${node}</th>"
              desc <<   "<td align='right'><font color=red>${String.format("%.2f",freeSpace)}G</font></td>"
              desc << "</tr>"
            }
            desc << "</table>"
            desc << "</body>"
            desc << "</html>"
        }

        return desc.toString()
   }
}