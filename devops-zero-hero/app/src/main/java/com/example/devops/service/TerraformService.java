package com.example.devops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.ResourceChange;
import com.example.devops.model.TerraformResult;

/**
 * Terraform plan/apply/destroy simulator. The declared infrastructure is fixed;
 * a change to the "desired state" produces a plan diff the learner can apply
 * or destroy — declarative infra, reviewed like code.
 */
@Service
public class TerraformService {

	public TerraformResult run(String mode, String change) {
		String how = mode == null || mode.isBlank() ? "plan" : mode.toLowerCase();
		String what = change == null || change.isBlank() ? "none" : change.toLowerCase();
		boolean destroy = how.equals("destroy");

		List<ResourceChange> changes = new ArrayList<>();
		List<String> steady = List.of(
				"aws_vpc.app_vpc",
				"aws_db_subnet_group.app_dbs",
				"aws_security_group.api",
				"aws_ecr_repository.api_registry",
				"aws_eks_cluster.api_cluster",
				"aws_eks_node_group.api_nodes",
				"aws_iam_role.api_role");

		if (destroy) {
			for (String r : steady) {
				changes.add(new ResourceChange(r, "destroy", "terraform destroy"));
			}
			changes.add(new ResourceChange("aws_rds_cluster.app_db", "destroy", "terraform destroy"));
			changes.add(new ResourceChange("aws_lb.app_alb", "destroy", "terraform destroy"));
			changes.add(new ResourceChange("aws_ec2_instance.legacy_web", "destroy", "terraform destroy"));
			return new TerraformResult(changes, 0, "destroy",
					"Destroy plan: the whole environment tears down in dependency order — no stale cloud bills, "
							+ "and the same code rebuilds it tomorrow.");
		}

		if (what.equals("db")) {
			changes.add(new ResourceChange("aws_rds_cluster.app_db", "add",
					"adding managed Postgres for the API"));
			if (how.equals("plan")) {
				return new TerraformResult(changes, steady.size(), "plan",
						("Planned: %d unchanged, 1 to add. Terraform shows you the diff before touching anything — "
								+ "review it like a pull request.").formatted(steady.size()));
			}
			return new TerraformResult(changes, steady.size(), "apply",
					"Applied: the RDS cluster is now provisioned and the plan matches reality.");
		}
		if (what.equals("scale")) {
			changes.add(new ResourceChange("aws_eks_node_group.api_nodes", "change",
					"desired_size 2 → 4"));
			if (how.equals("plan")) {
				return new TerraformResult(changes, steady.size() - 1, "plan",
						"Planned: %d unchanged, 1 to update — a scale-out, applied without rebooting the cluster"
								.formatted(steady.size() - 1));
			}
			return new TerraformResult(changes, steady.size() - 1, "apply",
					"Applied: node group now targets 4 nodes; the autoscaler reconciles from here.");
		}
		return new TerraformResult(List.of(), steady.size(), how,
				("Planned: %d modules unchanged — reality already matches the desired state. Terraform's job is "
						+ "having nothing to do.").formatted(steady.size()));
	}
}